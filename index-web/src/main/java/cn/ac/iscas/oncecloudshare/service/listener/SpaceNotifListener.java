package cn.ac.iscas.oncecloudshare.service.listener;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity;
import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity.EntityType;
import cn.ac.iscas.oncecloudshare.service.service.SpaceIndexService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

/**
 * 监听JMS消息
 * 
 * @author Chen Hao
 */
public class SpaceNotifListener implements MessageListener {

	private static Logger logger = LoggerFactory.getLogger(SpaceNotifListener.class);

	public SpaceNotifListener() {
	}
	
	private Gson gson = Gsons.defaultGson();

	@Resource
	private SpaceIndexService spaceIndexService;

	@Override
	public void onMessage(Message message) {
		try {
			
			if (!(message instanceof TextMessage)) {
				return;
			}
			
			TextMessage msg = (TextMessage) message;

			IndexEntity<?> entity = gson.fromJson(msg.getText(),IndexEntity.class);
			
			checkNotNull(entity);
			
			EntityType entityType = entity.getEntityType();

			if (entityType.equals(EntityType.CREATE_OR_UPDATE))
				spaceIndexService.updateIndex(entity);
			else
				spaceIndexService.deleteIndex(entity);
		} catch (Exception e) {
			logger.error("Error processing notification message", e);
		}
	}

}
