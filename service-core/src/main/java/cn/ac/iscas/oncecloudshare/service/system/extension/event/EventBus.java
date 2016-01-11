/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ac.iscas.oncecloudshare.service.system.extension.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.DeadEvent;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;


public class EventBus {
	
	private static Logger logger=LoggerFactory.getLogger(EventBus.class);

	/**
	 * A thread-safe cache for flattenHierarchy(). The Class class is immutable.
	 * This cache is shared across all EventBus instances, which greatly
	 * improves performance if multiple such instances are created and objects
	 * of the same class are posted on all of them.
	 */
	private static final LoadingCache<Class<?>,Set<Class<?>>> flattenHierarchyCache=CacheBuilder
			.newBuilder().weakKeys()
			.build(new CacheLoader<Class<?>,Set<Class<?>>>(){

				@SuppressWarnings({"unchecked", "rawtypes"})
				// safe cast
				@Override
				public Set<Class<?>> load(Class<?> concreteClass){
					return (Set)TypeToken.of(concreteClass).getTypes()
							.rawTypes();
				}
			});

	/**
	 * All registered event handlers, indexed by event type.
	 * 
	 * <p>
	 * This SetMultimap is NOT safe for concurrent use; all access should be
	 * made after acquiring a read or write lock via {@link #handlersByTypeLock}.
	 */
	private final SetMultimap<Class<?>,EventHandler> handlersByType=HashMultimap
			.create();
	private final ReadWriteLock handlersByTypeLock=new ReentrantReadWriteLock();

	/** queues of events for the current thread to dispatch */
	private final ThreadLocal<Queue<EventWithHandler>> eventsToDispatch=new ThreadLocal<Queue<EventWithHandler>>(){

		@Override
		protected Queue<EventWithHandler> initialValue(){
			return new LinkedList<EventWithHandler>();
		}
	};

	/** true if the current thread is currently dispatching an event */
	private final ThreadLocal<Boolean> isDispatching=new ThreadLocal<Boolean>(){

		@Override
		protected Boolean initialValue(){
			return false;
		}
	};
	
	ExtensionManager extManager;

	public EventBus(ExtensionManager extManager){
		this.extManager=extManager;
	}

	
	/**
	 * 注册非extension的listener。
	 * 
	 * @param listener
	 */
	public void register(Object listener){
		register(null,listener);
	}
	
	
	/**
	 * 注册extension的listener。
	 * 
	 * 原始注释：
	 * Registers all handler methods on {@code object} to receive events.
	 * Handler methods are selected and classified using this EventBus's
	 * {@link HandlerFindingStrategy}; the default strategy is the
	 * {@link AnnotatedHandlerFinder}.
	 * 
	 * @param extName extension的名称，用于判断listener是否有效
	 * @param listener listener对象
	 */
	public void register(String extName,Object listener){
		Multimap<Class<?>,EventHandler> methodsInListener=
				createHandlers(extName,listener);
		handlersByTypeLock.writeLock().lock();
		try{
			handlersByType.putAll(methodsInListener);
		}
		finally{
			handlersByTypeLock.writeLock().unlock();
		}
	}
	
	private Multimap<Class<?>,EventHandler> createHandlers(
			String extName,Object listener){
		Multimap<Class<?>,EventHandler> methodsInListener=HashMultimap.create();
		Class<?> clazz=listener.getClass();
		for(Method method: getAnnotatedMethods(clazz)){
			Class<?>[] parameterTypes=method.getParameterTypes();
			Class<?> eventType=parameterTypes[0];
			EventHandler handler=makeHandler(extName,listener,method);
			methodsInListener.put(eventType,handler);
		}
		return methodsInListener;
	}
	
	private static ImmutableList<Method> getAnnotatedMethods(
			Class<?> clazz){
		Set<? extends Class<?>> supers=TypeToken.of(clazz).getTypes()
				.rawTypes();
		ImmutableList.Builder<Method> result=ImmutableList.builder();
		for(Method method: clazz.getMethods()){
			/*
			 * 检查该class及其父类的方法中有没有@SubscribeEvent
			 */
			for(Class<?> c: supers){
				try{
					Method m=c.getMethod(method.getName(),
							method.getParameterTypes());
					if(m.isAnnotationPresent(SubscribeEvent.class)){
						Class<?>[] parameterTypes=method.getParameterTypes();
						if(parameterTypes.length!=1){
							throw new IllegalArgumentException("listener method "+
									method+" must have only one parameter");
						}
//						Class<?> eventType=parameterTypes[0];
						result.add(method);
						break;
					}
				}
				catch(NoSuchMethodException ignored){
					// Move on.
				}
			}
		}
		return result.build();
	}
	
	/**
	 * Creates an {@code EventHandler} for subsequently calling {@code method}
	 * on {@code listener}. Selects an EventHandler implementation based on the
	 * annotations on {@code method}.
	 * 
	 * @param listener
	 *            object bearing the event handler method.
	 * @param method
	 *            the event handler method to wrap in an EventHandler.
	 * @return an EventHandler that will call {@code method} on {@code listener}
	 *         when invoked.
	 */
	private static EventHandler makeHandler(String extName,
			Object listener,Method method){
		EventHandler wrapper;
		if(method.getAnnotation(SubscribeEvent.class)!=null){
			//如果方法被标记为线程安全的
			wrapper=new EventHandler(extName,listener,method);
		}
		else{
			wrapper=new SynchronizedEventHandler(extName,listener,method);
		}
		return wrapper;
	}


	/**
	 * Posts an event to all registered handlers. This method will return
	 * successfully after the event has been posted to all handlers, and
	 * regardless of any exceptions thrown by handlers.
	 * 
	 * <p>
	 * If no handlers have been subscribed for {@code event}'s class, and
	 * {@code event} is not already a {@link DeadEvent}, it will be wrapped in a
	 * DeadEvent and reposted.
	 * 
	 * @param event
	 *            event to post.
	 */
	public void post(Object event){
		Set<Class<?>> dispatchTypes=flattenHierarchy(event.getClass());

//		boolean dispatched=false;
		for(Class<?> eventType: dispatchTypes){
			handlersByTypeLock.readLock().lock();
			try{
				Set<EventHandler> wrappers=handlersByType.get(eventType);

				if(!wrappers.isEmpty()){
//					dispatched=true;
					for(EventHandler wrapper: wrappers){
						enqueueEvent(event,wrapper);
					}
				}
			}
			finally{
				handlersByTypeLock.readLock().unlock();
			}
		}

		dispatchQueuedEvents();
	}

	/**
	 * Queue the {@code event} for dispatch during
	 * {@link #dispatchQueuedEvents()}. Events are queued in-order of occurrence
	 * so they can be dispatched in the same order.
	 */
	void enqueueEvent(Object event,EventHandler handler){
		eventsToDispatch.get().offer(new EventWithHandler(event,handler));
	}

	/**
	 * Drain the queue of events to be dispatched. As the queue is being
	 * drained, new events may be posted to the end of the queue.
	 */
	void dispatchQueuedEvents(){
		// don't dispatch if we're already dispatching, that would allow
		// reentrancy
		// and out-of-order events. Instead, leave the events to be dispatched
		// after the in-progress dispatch is complete.
		if(isDispatching.get()){
			return;
		}

		isDispatching.set(true);
		try{
			Queue<EventWithHandler> events=eventsToDispatch.get();
			EventWithHandler eventWithHandler;
			while((eventWithHandler=events.poll())!=null){
				dispatch(eventWithHandler.event,eventWithHandler.handler);
			}
		}
		finally{
			isDispatching.remove();
			eventsToDispatch.remove();
		}
	}

	/**
	 * Dispatches {@code event} to the handler in {@code wrapper}. This method
	 * is an appropriate override point for subclasses that wish to make event
	 * delivery asynchronous.
	 * 
	 * @param event
	 *            event to dispatch.
	 * @param wrapper
	 *            wrapper that will call the handler.
	 */
	void dispatch(Object event,EventHandler wrapper){
		try{
			/*
			 * 如果extName为null，表示listener不属于任何插件，可以直接运行
			 */
			if(wrapper.extName==null ||
					extManager.isExtensionEnabled(wrapper.extName)){
				wrapper.handleEvent(event);
			}
		}
		catch(InvocationTargetException e){
			//检查是否允许抛出异常
			boolean allowIntercept=event.getClass()
					.getAnnotation(Interceptable.class)!=null;
			if(allowIntercept){
				if(e.getTargetException()!=null){
					if(e.getTargetException() instanceof RestException){
						throw (RestException)e.getTargetException();
					}
					else if(e.getTargetException() instanceof BusinessException){
						throw (BusinessException)e.getTargetException();
					}
				}
			}
			logger.info("exception occurs when dispatching event["+event
					+"] to extension["+wrapper.extName+"]: "
					+e.getTargetException().getMessage());
		}
	}

	/**
	 * Flattens a class's type hierarchy into a set of Class objects. The set
	 * will include all superclasses (transitively), and all interfaces
	 * implemented by these superclasses.
	 * 
	 * @param concreteClass
	 *            class whose type hierarchy will be retrieved.
	 * @return {@code clazz}'s complete type hierarchy, flattened and uniqued.
	 */
	@VisibleForTesting
	Set<Class<?>> flattenHierarchy(Class<?> concreteClass){
		try{
			return flattenHierarchyCache.getUnchecked(concreteClass);
		}
		catch(UncheckedExecutionException e){
			throw Throwables.propagate(e.getCause());
		}
	}

	/** simple struct representing an event and it's handler */
	static class EventWithHandler {

		final Object event;
		final EventHandler handler;

		public EventWithHandler(Object event, EventHandler handler){
			this.event=checkNotNull(event);
			this.handler=checkNotNull(handler);
		}
	}
}
