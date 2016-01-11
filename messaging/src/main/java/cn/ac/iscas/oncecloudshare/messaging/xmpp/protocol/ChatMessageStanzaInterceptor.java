package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import org.apache.vysper.xml.fragment.XMLText;
import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.MessageStanza;
import org.apache.vysper.xmpp.stanza.MessageStanzaType;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.dto.chat.ChatMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.ChatMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.service.Services;
import cn.ac.iscas.oncecloudshare.messaging.service.authc.AccountService;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 拦截单聊消息
 * 
 * @author Chen Hao
 */
public final class ChatMessageStanzaInterceptor extends MessageStanzaInterceptor{
	
	private static Logger logger=LoggerFactory.getLogger(ChatMessageStanzaInterceptor.class);
	
	private static final String[] deserilizeFields={"type","content"};
//	private static final String[] serilizeFields={"type","content","sender","receiver","id","createTime","seq"};
	
	private Gson deserializeGson=Gsons.filterByFields(
			ChatMessage.class,Lists.newArrayList(deserilizeFields));
	private Gson serializeGson=Gsons.defaultGsonNoPrettify();
	
	/**
	 * 是否是单聊消息的stanza
	 * @param stanza
	 * @return
	 */
	public static boolean isChatMessageStanza(Stanza stanza){
		if(!MessageStanza.isOfType(stanza)){
			return false;
		}
		if(NotifMessageStanzaInterceptor.isNotifMessageStanza(stanza)){
			return false;
		}
		MessageStanzaType type=new MessageStanza(stanza).getMessageType();
		if(type.equals(MessageStanzaType.NORMAL) || 
				type.equals(MessageStanzaType.CHAT)){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public boolean shouldIntercept(Stanza stanza){
		return isChatMessageStanza(stanza);
	}

	@Override
	public boolean preProcess(Stanza stanza,SessionContext sessionContext){
		try{
			MessageStanza mStanza=new MessageStanza(stanza);
			
			String body=mStanza.getBody(null);
			if(Strings.isNullOrEmpty(body)){
				return false;
			}
			
			TenantUser from=JIDUtil.parseTenantUser(mStanza.getFrom());
			TenantUser to=JIDUtil.parseTenantUser(mStanza.getTo());
			
			if(from==null || to==null){
				return false;
			}
			if(!Services.getTenantService().verifyTenantExist(from.getTenantId())){
				return false;
			}
			if(from.getTenantId()!=to.getTenantId()){
				return false;
			}
			
			AccountService aService=Services.getAccountService();
			if(!aService.verifyUserExists(from) ||
					!aService.verifyUserExists(to)){
				return false;
			}
			
			ChatMessage message=null;
			try{
				message=deserializeGson.fromJson(body,ChatMessage.class);
				if(message.getType()==null){
					return false;
				}
				if(Strings.isNullOrEmpty(message.getContent())){
					return false;
				}
			}
			catch(JsonSyntaxException e){
				//simply omit invalid stanza 
				return false;
			}
			message.setSender(from.getUserId());
			message.setReceiver(to.getUserId());
			
			Services.getChatMessageService()
				.save(from.getTenantId(),message);
			
			XMLText bodyXmlText=mStanza.getBodies().get(null)
					.getSingleInnerText();
			modifyXMLText(bodyXmlText,
					serializeGson.toJson(ChatMessageDto.of(message)));
			
			return true;
		}
		catch(Exception e){
			logger.warn("invalid im message stanza: "+stanza.toString(),e);
			return false;
		}
	}
	
}
