package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import java.lang.reflect.Field;

import org.apache.vysper.xml.fragment.XMLText;
import org.apache.vysper.xmpp.stanza.MessageStanza;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class MessageStanzaInterceptor implements StanzaInterceptor {
	
	@SuppressWarnings("unused")
	private static Logger logger=LoggerFactory.getLogger(MessageStanzaInterceptor.class);

	@Override
	public boolean shouldIntercept(Stanza stanza){
		return MessageStanza.isOfType(stanza);
	}
	
//	@Override
//	public boolean preProcess(Stanza stanza){
//		try{
//			MessageStanza mStanza=new MessageStanza(stanza);
//			Entity from=mStanza.getFrom();
//			Entity to=mStanza.getTo();
//			String body=mStanza.getBody(null);
//			if(from==null || to==null || Strings.isNullOrEmpty(body)){
////				throw new IllegalArgumentException();
//				return true;
//			}
//			XMLText bodyXmlText=mStanza.getBodies().get(null).getSingleInnerText();
//			modifyXMLText(bodyXmlText,body+" hacked :)");
//			
//			logger.debug(from+","+to+": "+body);
//			return true;
//		}
//		catch(Exception e){
//			logger.warn("invalid message stanza: "+stanza.toString(),e);
//			return false;
//		}
//	}

	/**
	 * modify XMLText via reflection
	 * 
	 * @param xmlText
	 * @param newText
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	protected boolean modifyXMLText(XMLText xmlText,String newText)
			throws IllegalArgumentException,IllegalAccessException,
			SecurityException,NoSuchFieldException{
		Field field=xmlText.getClass().getDeclaredField("text");
		field.setAccessible(true);
		field.set(xmlText,newText);
		return true;
	}
}
