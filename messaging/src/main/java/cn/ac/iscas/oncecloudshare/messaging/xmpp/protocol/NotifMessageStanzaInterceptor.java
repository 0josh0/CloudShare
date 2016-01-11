package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.MessageStanza;
import org.apache.vysper.xmpp.stanza.Stanza;

import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;

/**
 * 拦截通知消息
 * 
 * @author Chen Hao
 */
public final class NotifMessageStanzaInterceptor extends MessageStanzaInterceptor {

	public static boolean isNotifMessageStanza(Stanza stanza){
		return MessageStanza.isOfType(stanza) && 
				JIDUtil.isNotifEntity(stanza.getFrom());
	}
	
	@Override
	public boolean shouldIntercept(Stanza stanza){
		return isNotifMessageStanza(stanza);
	}
	
	@Override
	public boolean preProcess(Stanza stanza,SessionContext sessionContext){
		return true;
	}
}
