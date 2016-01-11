package cn.ac.iscas.oncecloudshare.messaging.service.notif;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

/**
 * 监听JMS消息
 *
 * @author Chen Hao
 */
public class NotifListener implements MessageListener {

	private static Logger logger=LoggerFactory.getLogger(NotifListener.class);
	
	public NotifListener(){
	}
	
	@Override
	public void onMessage(Message message){
		try{
			if(!(message instanceof MapMessage)){
				return;
			}
			MapMessage msg=(MapMessage)message;
			String content=msg.getString("content");
			String type=msg.getString("type");
			String to=Strings.nullToEmpty(msg.getString("to"));
			String attributes=Strings.nullToEmpty(msg.getString("attributes"));
			long tenantId=msg.getLong("tenantId");
			
			List<Long> toList=Lists.newArrayList();
			for(String str:Splitter.on(',')
					.omitEmptyStrings().split(to)){
				Long id=Longs.tryParse(str);
				if(id!=null){
					toList.add(id);
				}
			}
			
			checkArgument(!Strings.isNullOrEmpty(content));
			checkArgument(!Strings.isNullOrEmpty(type));
			checkArgument(!toList.isEmpty());
			
			NotifMessage notif=new NotifMessage(type,content,attributes);
			NotifSender.getInstance().sendNotif(tenantId,notif,toList);
		}
		catch(Exception e){
			logger.error("Error processing received notif",e);
		}
	}

}
