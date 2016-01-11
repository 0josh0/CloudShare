package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.Stanza;

@Deprecated
public class InvalidMessageStanzaInterceptor extends MessageStanzaInterceptor{

	@Override
	public boolean shouldIntercept(Stanza stanza){
//		if(!MessageStanza.isOfType(stanza)){
//			return  false;
//		}
//		
//		if(JIDUtil.isNotifEntity(stanza.getFrom())){
//			return false;
//		}
//		if(JIDUtil.isValidUserEntity(stanza.getFrom()) &&
//				JIDUtil.isValidUserEntity(stanza.getTo()) ){
//			return false;
//		}
//
//		//non-notif & non-im MessageStanza
//		return true;
		return false;
	}
	
	@Override
	public boolean preProcess(Stanza stanza,SessionContext sessionContext){
		return false;
	}
}
