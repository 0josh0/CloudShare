package cn.ac.iscas.oncecloudshare.messaging.service.notif;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springside.modules.mapper.BeanMapper;

import cn.ac.iscas.oncecloudshare.messaging.dto.notif.NotifMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;
import cn.ac.iscas.oncecloudshare.messaging.service.Services;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;

import com.google.gson.Gson;

@Service(value="notifSender")
public class NotifSender {
	
	private static Logger logger=LoggerFactory.getLogger(NotifSender.class);

	private static NotifSender instance;
	
	private Gson gson=Gsons.defaultGsonNoPrettify();
	
	@Autowired
	NotifMessageService nmService;
	
	private boolean initialized=false;
	
	private NotifSender(){
		instance=this;
	}
	
	public static NotifSender getInstance(){
		if(!instance.initialized){
			synchronized(NotifSender.class){
				if(!instance.initialized){
					try{
						instance.init();
						instance.initialized=true;
					}
					catch(Exception e){
						throw new RuntimeException("error initializing NotifSender",e);
					}
				}
			}
		}
		return instance;
	}
	
	@Value(value="${notif.username}")
	private String notifUsername;
	
	@Value(value="${notif.password}")
	private String notifPassword;
	
	@Value(value="${domain}")
	private String domain;
	
	@Value(value="${xmpp.port}")
	private int xmppPort;
	
	private Entity notifEntity;
	
	XMPPConnection conn;
	
	ExecutorService executor;
	
	private void init() throws Exception{
		notifEntity=EntityImpl.parse(notifUsername+"@"+domain);
		
		ConnectionConfiguration config = new ConnectionConfiguration("localhost",xmppPort);
        config.setCompressionEnabled(false);
        config.setSelfSignedCertificateEnabled(true);
        config.setExpiredCertificatesCheckEnabled(false);
        config.setDebuggerEnabled(false);
        config.setSASLAuthenticationEnabled(true);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        conn = new XMPPConnection(config);
        conn.connect();
        conn.login(notifEntity.getBareJID().toString(),notifPassword);
        conn.getRoster().setSubscriptionMode(Roster.SubscriptionMode.reject_all);
        
		int coreThreadCount=5;
		int maxThreadCount=50;
		int threadTimeoutSeconds=1*60;
		this.executor=new ThreadPoolExecutor(coreThreadCount, maxThreadCount,
				threadTimeoutSeconds, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	@PreDestroy
	public void destroy(){
		conn.disconnect();
	}
	
	/**
	 * send notification to users in the list
	 * 
	 * @param notif
	 * @param toList
	 */
	public void sendNotif(long tenantId,NotifMessage notif,List<Long> toList){
		executor.submit(new NotificationTask(tenantId,notif,toList));
	}
	
	private class NotificationTask implements Runnable{

		long tenantId;
		NotifMessage notif;
		List<Long> toList;
		
		public NotificationTask(long tenantId,NotifMessage notif, List<Long> toList){
			this.tenantId=tenantId;
			this.notif=notif;
			this.toList=toList;
		}

		@Override
		public void run(){
			logger.debug("send notif to {}, content:{}",toList,notif.getContent());
			for(Long toId:toList){
				try{
					if(!Services.getAccountService().verifyUserExists(new TenantUser(tenantId,toId))){
						continue;
					}
					//save to db
					NotifMessage tmp=BeanMapper.map(notif,NotifMessage.class);
					tmp.setReceiver(toId);
					nmService.save(tenantId,tmp);
					
					//send to client
					String to=JIDUtil.buildEntity(tenantId,toId).toString();
					Chat chat=conn.getChatManager().createChat(to,null);
					chat.sendMessage(gson.toJson(NotifMessageDto.of(tmp)));
				}
				catch(Exception e){
					logger.error("error sending notif to {} : {}",
							toId,gson.toJson(notif));
					logger.error("",e);
				}
			}
		}
		
	}
}
