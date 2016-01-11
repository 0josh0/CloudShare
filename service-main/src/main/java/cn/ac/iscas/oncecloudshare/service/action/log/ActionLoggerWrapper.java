package cn.ac.iscas.oncecloudshare.service.action.log;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;

public class ActionLoggerWrapper {
	private static final Logger _logger = LoggerFactory.getLogger(ActionLoggerWrapper.class);
	
	private Object logger;
	private Method method;
	
	public ActionLoggerWrapper(Object logger, Method method){
		Preconditions.checkNotNull(logger,"ActionLoggerWrapper logger cannot be null.");
		Preconditions.checkNotNull(method,"ActionLoggerWrapper method cannot be null.");
		this.logger = logger;
		this.method = method;
	}
	
	public boolean wrap(Object object, ActionLog actionLog){
		try {
			return (Boolean) method.invoke(logger, object, actionLog);
		} catch (Exception e) {
			_logger.error(null, e);
			return true;
		}
	}
}