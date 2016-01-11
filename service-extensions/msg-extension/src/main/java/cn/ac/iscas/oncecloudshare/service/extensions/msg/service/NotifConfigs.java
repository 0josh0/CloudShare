package cn.ac.iscas.oncecloudshare.service.extensions.msg.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;

@Component
public class NotifConfigs {

	@Resource(name = "globalConfigService")
	private ConfigService<?> cService;
	
	public String getJmsUsername(){
		return cService.getConfig(Keys.JMS_USERNAME,"");
	}
	
	public String getJmsPassword(){
		return cService.getConfig(Keys.JMS_PASSWORD,"");
	}
	
	public String getJmsBrokerUrl(){
		return cService.getConfig(Keys.JMS_BROKER_URL,"");
	}
	
	public String getJmsQueueName(){
		return cService.getConfig(Keys.JMS_QUEUE,"");
	}
	
	public String getMsgServerUrl(){
		return cService.getConfig(Keys.MSG_SERVER_URL, StringUtils.EMPTY);
	}
	
	private static final class Keys{
		static String JMS_USERNAME="msg.jms.username";
		static String JMS_PASSWORD="msg.jms.password";
		static String JMS_BROKER_URL="msg.jms.brokerUrl";
		static String JMS_QUEUE="msg.jms.queue";
		
		static String MSG_SERVER_URL = "msg.server.url";
	}
}
