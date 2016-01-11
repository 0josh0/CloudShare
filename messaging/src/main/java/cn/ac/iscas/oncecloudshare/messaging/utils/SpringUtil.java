package cn.ac.iscas.oncecloudshare.messaging.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext){
		SpringUtil.applicationContext=applicationContext;
	}

	public static Object getBean(String name) throws BeansException{
		return applicationContext.getBean(name);
	}

	@SuppressWarnings ("unchecked")
	public static <T> T getBean(Class<?> clazz) throws BeansException{
		return (T)applicationContext.getBean(clazz);
	}
}