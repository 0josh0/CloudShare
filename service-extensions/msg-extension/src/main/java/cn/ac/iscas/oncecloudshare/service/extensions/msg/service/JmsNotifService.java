package cn.ac.iscas.oncecloudshare.service.extensions.msg.service;

import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.service.common.NotifService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

/**
 * send notif via JMS
 * 
 * @author Chen Hao
 */
@Service
public class JmsNotifService implements NotifService {
	private static Logger logger = LoggerFactory.getLogger(JmsNotifService.class);
	private Gson gson = Gsons.gsonForLogging();

	@Autowired
	NotifConfigs notifConfigs;

	JmsTemplate jmsTemplate;

	Destination destination;
	@Resource
	private TenantService tenantService;

	boolean initialized = false;

	private void init() {
		if (initialized) {
			return;
		}
		synchronized (this) {
			String username = notifConfigs.getJmsUsername();
			String password = notifConfigs.getJmsPassword();
			String brokerUrl = notifConfigs.getJmsBrokerUrl();
			String queueName = notifConfigs.getJmsQueueName();

			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(username, password, brokerUrl);
			jmsTemplate = new JmsTemplate(factory);
			destination = new ActiveMQQueue(queueName);
			initialized = true;
		}
	}

	@Override
	public void sendNotif(Notification notification) {
		init();
		sendNotifInternal(destination, notification);
	}

	private void sendNotifInternal(Destination destination, Notification notification) {
		try {
			String toStr = "";
			if (notification.getTo() != null) {
				toStr = Joiner.on(',').skipNulls().join(notification.getTo());
			}
			Map<String, Object> map = Maps.newHashMap();
			map.put("to", toStr);
			if (StringUtils.isNotEmpty(notification.getContent())) {
				map.put("content", notification.getContent());
			}
			if (StringUtils.isNotEmpty(notification.getAttributes())) {
				map.put("attributes", notification.getAttributes());
			}
			map.put("type", notification.getType().getType());
			map.put("tenantId", tenantService.getCurrentTenant().getId());
			jmsTemplate.convertAndSend(destination, map);
		} catch (JmsException e) {
			logger.error("Error sending notif: {}.", gson.toJson(notification));
		}
	}
}
