package cn.ac.iscas.oncecloudshare.messaging.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class Messages {

	@Autowired
	private MessageSource messageSource;
	
	private static Messages instance;
	
	private Messages(){
		instance=this;
	}
	
	public static String getMessage(String code){
		return instance.messageSource.getMessage(code,null,null);
	}
	
	public static String getMessageOrDefault(String code,String defaultValue){
		return instance.messageSource.getMessage(code,null,defaultValue,null);
	}
}
