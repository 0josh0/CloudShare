package cn.ac.iscas.oncecloudshare.service.extensions.index.service;

import javax.annotation.PostConstruct;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.gson.Gson;

/**
 * send index via JMS
 * 
 * @author One
 */
@Service
public class IndexNotifyService {
	private static Logger logger = LoggerFactory.getLogger(IndexNotifyService.class);
	private Gson gson = Gsons.defaultGson();

	@Autowired
	IndexConfigs notifConfigs;

	JmsTemplate jmsTemplate;

	Destination destination;

	@PostConstruct
	private void init() {

		String username = notifConfigs.getJMS_USERNAME();
		String password = notifConfigs.getJMS_PASSWORD();
		String brokerUrl = notifConfigs.getJMS_BROKER_URL();
		String queueName = notifConfigs.getPER_QUEUE();

		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(username, password, brokerUrl);
		jmsTemplate = new JmsTemplate(factory);
		destination = new ActiveMQQueue(queueName);
	}

	public synchronized void sendNotif(IndexEntity<?> notification) {
		sendNotifInternal(destination, gson.toJson(notification));
	}

	private void sendNotifInternal(Destination destination, String notification) {
		try {
			
			jmsTemplate.convertAndSend(destination, notification);
		} catch (JmsException e) {
			logger.error("Error sending notif: {}.", gson.toJson(notification));
		}
	}

}
