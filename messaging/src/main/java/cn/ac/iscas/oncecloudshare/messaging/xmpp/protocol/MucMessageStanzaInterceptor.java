package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import org.apache.vysper.xml.fragment.XMLText;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.MessageStanza;
import org.apache.vysper.xmpp.stanza.MessageStanzaType;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.dto.muc.MucMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantRoom;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.service.Services;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 拦截群聊消息
 * 
 * @author Chen Hao
 */
public final class MucMessageStanzaInterceptor extends MessageStanzaInterceptor{
	
	private static Logger logger=LoggerFactory.getLogger(MucMessageStanzaInterceptor.class);
	
	private static final String[] deserilizeFields={"type","content"};
	
	private Gson deserializeGson=Gsons.filterByFields(
			MucMessage.class,Lists.newArrayList(deserilizeFields));
	private Gson serializeGson=Gsons.defaultGsonNoPrettify();

	public static boolean isMucMessageStanza(Stanza stanza){
		if(!MessageStanza.isOfType(stanza)){
			return false;
		}
		MessageStanzaType type=new MessageStanza(stanza).getMessageType();
		if(type.equals(MessageStanzaType.GROUPCHAT)){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public boolean shouldIntercept(Stanza stanza){
		return isMucMessageStanza(stanza);
	}

	@Override
	public boolean preProcess(Stanza stanza,SessionContext sessionContext){
		try{
			MessageStanza mStanza=new MessageStanza(stanza);
			
			String body=mStanza.getBody(null);
			if(Strings.isNullOrEmpty(body)){
				return false;
			}
			
			Entity from=mStanza.getFrom();
			if(from==null){
				from=sessionContext.getInitiatingEntity();
			}
			TenantUser sender=JIDUtil.parseTenantUser(from);
			TenantRoom room=JIDUtil.parseTenantRoom(mStanza.getTo());
			
			if(sender==null || room==null){
				return false;
			}
			if(!Services.getTenantService().verifyTenantExist(sender.getTenantId())){
				return false;
			}
			//如果不是属于同一个tenant
			if(sender.getTenantId()!=room.getTenantId()){
				return false;
			}
			
			//如果不在该room
			OccupantService oService=Services.getOccupantService();
			if(oService.find(room.getTenantId(),room.getRoomId(),sender.getUserId())==null){
				return false;
			}
			
			MucMessage message=null;
			
			try{
				message=deserializeGson.fromJson(body,MucMessage.class);
				if(message.getType()==null){
					return false;
				}
				if(Strings.isNullOrEmpty(message.getContent())){
					return false;
				}
			}
			catch(JsonSyntaxException e){
				return false;
			}
			message.setSender(sender.getUserId());
			message.setRoomId(room.getRoomId());
			
			Services.getMucMessageService()
				.save(room.getTenantId(),message);
			
			XMLText bodyXmlText=mStanza.getBodies().get(null)
					.getSingleInnerText();
			modifyXMLText(bodyXmlText,
					serializeGson.toJson(MucMessageDto.of(message)));
			
			return true;
		}
		catch(Exception e){
			logger.warn("invalid muc message stanza: "+stanza.toString(),e);
			return false;
		}
	}
}
