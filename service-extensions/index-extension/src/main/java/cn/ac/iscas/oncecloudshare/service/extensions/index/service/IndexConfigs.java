package cn.ac.iscas.oncecloudshare.service.extensions.index.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;

@Component
public class IndexConfigs {

	@Resource(name = "globalConfigService")
	private ConfigService<?> cService;
	
	public  String getJMS_USERNAME() {
		return cService.getConfig(Keys.JMS_USERNAME,"");
	}
	public  String getJMS_PASSWORD() {
		return cService.getConfig(Keys.JMS_PASSWORD,"");
	}
	public  String getJMS_BROKER_URL() {
		return cService.getConfig(Keys.JMS_BROKER_URL,"");
	}
	public  String getPER_QUEUE() {
		return cService.getConfig(Keys.PER_QUEUE,"");
	}
	public  String getSPACE_QUEUE() {
		return cService.getConfig(Keys.SPACE_QUEUE,"");
	}
	

	private static final class Keys {
		static String JMS_USERNAME = "msg.jms.username";
		static String JMS_PASSWORD = "msg.jms.password";
		static String JMS_BROKER_URL = "msg.jms.brokerUrl";
		static String PER_QUEUE = "jms.queue.personal.name";
		static String SPACE_QUEUE = "jms.queue.space.name";
	}

}
