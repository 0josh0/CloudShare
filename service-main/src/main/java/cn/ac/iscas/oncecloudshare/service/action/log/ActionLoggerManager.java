package cn.ac.iscas.oncecloudshare.service.action.log;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.action.log.annotations.ActionLogger;
import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;
import cn.ac.iscas.oncecloudshare.service.service.common.ActionLogService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;

@Component
public class ActionLoggerManager {
	private static final Logger _logger = LoggerFactory.getLogger(ActionLoggerManager.class);
	
	@Resource
	private ActionLogService actionLogService;
	@Resource
	private SystemLoggers systemLoggers;
	@Resource
	private RuntimeContext runtimeContext;

	/**
	 * A thread-safe cache for flattenHierarchy(). The Class class is immutable. This cache is shared across all EventBus
	 * instances, which greatly improves performance if multiple such instances are created and objects of the same class are
	 * posted on all of them.
	 */
	private static final LoadingCache<Class<?>, Set<Class<?>>> flattenHierarchyCache = CacheBuilder.newBuilder().weakKeys()
			.build(new CacheLoader<Class<?>, Set<Class<?>>>() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				// safe cast
				@Override
				public Set<Class<?>> load(Class<?> concreteClass) {
					return (Set) TypeToken.of(concreteClass).getTypes().rawTypes();
				}
			});
	private final SetMultimap<Class<?>, ActionLoggerWrapper> actionLoggers = HashMultimap.create();
	private final ReadWriteLock loggersLock = new ReentrantReadWriteLock();
	private final ThreadLocal<Queue<ObjectAndLoggers>> objectsToLog = new ThreadLocal<Queue<ObjectAndLoggers>>() {
		@Override
		protected Queue<ObjectAndLoggers> initialValue() {
			return new LinkedList<ObjectAndLoggers>();
		}
	};
	private final ThreadLocal<Boolean> isLogging = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	@PostConstruct
	public void init() {
		register(systemLoggers);
		runtimeContext.getEventBus().register(this);
	}

	public void register(Object loggers) {
		Multimap<Class<?>, ActionLoggerWrapper> wrappers = createWrappers(loggers);
		loggersLock.writeLock().lock();
		try {
			actionLoggers.putAll(wrappers);
		} finally {
			loggersLock.writeLock().unlock();
		}
	}

	private Multimap<Class<?>, ActionLoggerWrapper> createWrappers(Object loggers) {
		Multimap<Class<?>, ActionLoggerWrapper> wrappers = HashMultimap.create();
		Class<?> clazz = loggers.getClass();
		for (Method method : getAnnotatedMethods(clazz)) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			Class<?> loggableType = parameterTypes[0];
			ActionLoggerWrapper wrapper = new ActionLoggerWrapper(loggers, method);
			wrappers.put(loggableType, wrapper);
		}
		return wrappers;
	}

	private static ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
		Set<? extends Class<?>> supers = TypeToken.of(clazz).getTypes().rawTypes();
		ImmutableList.Builder<Method> result = ImmutableList.builder();
		for (Method method : clazz.getMethods()) {
			for (Class<?> c : supers) {
				try {
					Method m = c.getMethod(method.getName(), method.getParameterTypes());
					if (m.isAnnotationPresent(ActionLogger.class)) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length != 2) {
							throw new IllegalArgumentException("listener method " + method + " must have two parameters");
						}
						result.add(method);
						break;
					}
				} catch (NoSuchMethodException ignored) {
					// Move on.
				}
			}
		}
		return result.build();
	}

	@SubscribeEvent
	public void post(Object obj) {
		if (obj instanceof ActionLog) {
			enqueue(obj, null);
		} else {
			Collection<ActionLoggerWrapper> wrappers = getLoggerWrappers(obj.getClass());
			if (!wrappers.isEmpty()) {
				enqueue(obj, wrappers);
			}
		}
		doLog();
	}

	Collection<ActionLoggerWrapper> getLoggerWrappers(Class<?> clazz) {
		Collection<ActionLoggerWrapper> wrappers = Sets.newHashSet();
		loggersLock.readLock().lock();
		try {
			for (Class<?> type : flattenHierarchy(clazz)) {
				wrappers.addAll(actionLoggers.get(type));
			}
		} finally {
			loggersLock.readLock().unlock();
		}
		return wrappers;
	}

	/**
	 * Queue the {@code event} for dispatch during {@link #dispatchQueuedEvents()}. Events are queued in-order of occurrence so
	 * they can be dispatched in the same order.
	 */
	void enqueue(Object obj, Collection<ActionLoggerWrapper> loggers) {
		objectsToLog.get().offer(new ObjectAndLoggers(obj, loggers));
	}

	void doLog() {
		if (isLogging.get()) {
			return;
		}
		isLogging.set(true);
		try {
			Queue<ObjectAndLoggers> queue = objectsToLog.get();
			ObjectAndLoggers objectAndLoggers;
			while ((objectAndLoggers = queue.poll()) != null) {
				doLog(objectAndLoggers.obj, objectAndLoggers.loggers);
			}
		} finally {
			isLogging.remove();
			objectsToLog.remove();
		}
	}

	void doLog(Object object, Collection<ActionLoggerWrapper> loggers) {
		if (object instanceof ActionLog) {
			actionLogService.save((ActionLog) object);
		} else {
			ActionLog log = new ActionLog();
			for (ActionLoggerWrapper logger : loggers) {
				// wrapper可以中断日志的生成
				if (!logger.wrap(object, log)){
					return;
				}
			}
			if (StringUtils.isEmpty(log.getType()) || StringUtils.isEmpty(log.getTargetType())){
				_logger.warn("忽略日志保存，原因：type或targetType为空");
			} else {
				actionLogService.save(log);
			}
		}
	}

	/**
	 * Flattens a class's type hierarchy into a set of Class objects. The set will include all superclasses (transitively), and
	 * all interfaces implemented by these superclasses.
	 * 
	 * @param concreteClass class whose type hierarchy will be retrieved.
	 * @return {@code clazz}'s complete type hierarchy, flattened and uniqued.
	 */
	@VisibleForTesting
	Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
		try {
			return flattenHierarchyCache.getUnchecked(concreteClass);
		} catch (UncheckedExecutionException e) {
			throw Throwables.propagate(e.getCause());
		}
	}

	/** simple struct representing an event and it's handler */
	static class ObjectAndLoggers {
		final Object obj;
		final Collection<ActionLoggerWrapper> loggers;

		public ObjectAndLoggers(Object obj, Collection<ActionLoggerWrapper> loggers) {
			this.obj = checkNotNull(obj);
			this.loggers = checkNotNull(loggers);
		}
	}
}
