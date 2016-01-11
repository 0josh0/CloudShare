package cn.ac.iscas.oncecloudshare.messaging.xmpp.storage;

import java.util.Collection;
import org.apache.vysper.xmpp.modules.extension.xep0160_offline_storage.OfflineStorageProvider;
import org.apache.vysper.xmpp.stanza.MessageStanza;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.model.AbstractChatMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.Message;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.ChatMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;
import cn.ac.iscas.oncecloudshare.messaging.service.authc.AccountService;
import cn.ac.iscas.oncecloudshare.messaging.service.iospush.IOSPusher;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.utils.Messages;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.ChatMessageStanzaInterceptor;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.MucMessageStanzaInterceptor;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.NotifMessageStanzaInterceptor;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class OcsOfflineStorageProvider implements OfflineStorageProvider {
	
	private static Logger logger=LoggerFactory.getLogger(OcsOfflineStorageProvider.class);
	
	private AccountService aService;
	
	private IOSPusher iosPusher;
	
	private Gson gson=Gsons.defaultGson();

	public OcsOfflineStorageProvider(AccountService aService,IOSPusher iosPusher){
		this.aService=aService;
		this.iosPusher=iosPusher;
	}

	@Override
	public void receive(Stanza stanza){
		Class<? extends Message> messageClass=null;
		if(NotifMessageStanzaInterceptor.isNotifMessageStanza(stanza)){
			messageClass=NotifMessage.class;
		}
		else if(ChatMessageStanzaInterceptor.isChatMessageStanza(stanza)){
			messageClass=ChatMessage.class;
		}
		else if(MucMessageStanzaInterceptor.isMucMessageStanza(stanza)){
			messageClass=MucMessage.class;
		}
		else{
			return;
		}
		
		Message message=null;
		TenantUser from=null;
		TenantUser to=null;
		
		try{
			MessageStanza mStanza=new MessageStanza(stanza);
			to=JIDUtil.parseTenantUser(mStanza.getTo());
			if(messageClass.equals(ChatMessage.class)){
				from=JIDUtil.parseTenantUser(mStanza.getFrom());
			}
			else if(messageClass.equals(MucMessage.class)){
				from=JIDUtil.parseRoomUser(mStanza.getFrom());
			}
			String body=mStanza.getBody(null);
			message=gson.fromJson(body,messageClass);
		}
		catch(Exception e){
			logger.error("",e);
			return;
		}
		
		String content="";
		
		if(message instanceof AbstractChatMessage){
			String fromName=null;
			AbstractChatMessage acMessage=(AbstractChatMessage)message;
			switch(acMessage.getType()){
			case TEXT:
				content=message.getContent();
				break;
			case AUDIO:
				content=Messages.getMessage("push.audio");
				break;
			case PIC:
				content=Messages.getMessage("push.pic");
				break;
			case FILE:
				content=Messages.getMessage("push.file");
				break;
			case GEO:
				content=Messages.getMessage("push.geo");
				break;
			default:
				logger.warn("unreconized ChatMessageType: "+acMessage.getType());
				break;
			}
			if(from!=null){
				fromName=aService.getUserInfo(from).getName();
				content=fromName+": "+content;
			}
		}
		else if(message instanceof NotifMessage){
			content=message.getContent();
		}
		
		iosPusher.pushNotification(to.getTenantId(),content,to.getUserId());
	}

	@Override
	public Collection<Stanza> getStanzasForBareJID(String bareJID){
		return ImmutableList.of();
	}

}
