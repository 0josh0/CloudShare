package cn.ac.iscas.oncecloudshare.service.system.service;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class DefaultServiceProviderRegistry implements ServiceProviderRegistry{

	private static Logger logger=LoggerFactory.getLogger(DefaultServiceProviderRegistry.class);
	
	protected enum OverridePolicy {
        ERROR, WARN, ACCEPT
    };
	
    protected Map<Class<? extends ServiceProvider>, ServiceProvider> serviceMap=Maps.newHashMap();
    
    protected OverridePolicy overridePolicy = OverridePolicy.ACCEPT;
    
	@Override
	public <T> T retrieve(Class<? extends ServiceProvider> clazz){
		ServiceProvider serviceProvider=serviceMap.get(clazz);
		return (T)serviceProvider;
	}

	@Override
	public void add(ServiceProvider serviceProvider){
		Preconditions.checkNotNull(serviceProvider,"Service must not be NULL!");
		Class<? extends ServiceProvider> clazz=serviceProvider.getClass();
		
		for(Class<? extends ServiceProvider> spInterface
				:getServiceProviderInterfaces(clazz)){
			addInternal(spInterface,serviceProvider);
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public void add(String serviceProviderFullQualifiedClassname){
		Class<ServiceProvider> clazz;
		try{
			clazz=(Class<ServiceProvider>)Class
					.forName(serviceProviderFullQualifiedClassname);
		}
		catch(ClassCastException e){
			logger.info("not a ServiceProvider class: "
					+serviceProviderFullQualifiedClassname);
			return;
		}
		catch(ClassNotFoundException e){
			logger.info("could not load ServiceProvider class "
					+serviceProviderFullQualifiedClassname);
			return;
		}
		try{
			ServiceProvider serviceProvider=clazz.newInstance();
			add(serviceProvider);
		}
		catch(Exception e){
			logger.info("failed to instantiate ServiceProvider class "
					+serviceProviderFullQualifiedClassname);
			return;
		}
	}

	/**
	 * 找出这个class具体是什么ServiceProvider
	 * @param clazz
	 * @return
	 */
	private Set<Class<? extends ServiceProvider>> getServiceProviderInterfaces(Class<?> clazz){
		Set<Class<? extends ServiceProvider>> spInterfaces=Sets.newHashSet();
		
		while(clazz!=null){
			for(Class<?> theInterface:clazz.getInterfaces()){
				for(Class<?> parentInterface:theInterface.getInterfaces()){
					if(parentInterface==ServiceProvider.class){
						spInterfaces.add((Class<? extends ServiceProvider>)theInterface);
						break;
					}
				}
			}
			
			clazz=clazz.getSuperclass();
		}
		
		return spInterfaces;
	}
	
	private void addInternal(Class<? extends ServiceProvider> interfaceClass,
			ServiceProvider serviceProvider){
		ServiceProvider oldProvider=retrieve(interfaceClass);
		String oldClassName=null;
		if(oldProvider!=null){
			oldClassName=oldProvider.getClass().getCanonicalName();
		}
		
		switch(overridePolicy){
		case ACCEPT:
			if(oldClassName!=null){
				logger.info("replace service provider {} with {}",oldClassName,
						serviceProvider.getClass().getCanonicalName());
			}
			break;
		case WARN:
			if(oldClassName!=null){
				logger.warn("replace service provider {} with {}",oldClassName,
						serviceProvider.getClass().getCanonicalName());
			}
			break;
		case ERROR:
			if(oldClassName!=null){
				throw new IllegalStateException("service already registered for class"+
						interfaceClass.getCanonicalName());
			}
        default:
            throw new IllegalStateException("unknown override policy state: " + overridePolicy.name());
		}
		
		serviceMap.put(interfaceClass,serviceProvider);
	}
}
