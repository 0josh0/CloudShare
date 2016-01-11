package cn.ac.iscas.oncecloudshare.service.utils.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		SpringUtil.applicationContext = applicationContext;
	}

	public static Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<?> clazz) throws BeansException {
		if(applicationContext==null){
			return null;
		}
		return (T) applicationContext.getBean(clazz);
	}

	public static <T> Map<String, T> getBeansOfClass(Class<T> type) {
		return applicationContext.getBeansOfType(type);
	}

	/**
	 * 获取当前的请求
	 * 
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
	
	public static final String getParamOrHeader(String name){
		if (StringUtils.isEmpty(name)){
			return StringUtils.EMPTY;
		}
		HttpServletRequest request = getRequest();
		String value = request.getParameter(name);
		if (StringUtils.isEmpty(value)){
			value = request.getHeader(name);
		}
		return value;
	}
}