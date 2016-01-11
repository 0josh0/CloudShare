package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.Stanza;


public interface StanzaInterceptor {
	
	/**
	 * whether handle this stanza or not.
	 * 
	 * @param stanza
	 * @return
	 */
	public boolean shouldIntercept(Stanza stanza);

	/**
	 * called before the stanza being processed
	 * 
	 * @param stanza 
	 * @return <code>true</code> if stanza processing should continue
	 */
	public boolean preProcess(Stanza stanza,SessionContext sessionContext);
	
//	/**
//	 * called after the stanza being processed
//	 * 
//	 * @param stanza
//	 */
//	public void postProcess(Stanza stanza);
}
