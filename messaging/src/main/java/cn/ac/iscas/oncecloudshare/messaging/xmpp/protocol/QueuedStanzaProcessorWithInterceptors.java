package cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol;

import java.util.ArrayList;
import java.util.List;

import org.apache.vysper.xml.fragment.Attribute;
import org.apache.vysper.xmpp.protocol.QueuedStanzaProcessor;
import org.apache.vysper.xmpp.protocol.SessionStateHolder;
import org.apache.vysper.xmpp.protocol.StanzaProcessor;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class QueuedStanzaProcessorWithInterceptors extends QueuedStanzaProcessor {
	
	private static Logger logger=LoggerFactory.getLogger(QueuedStanzaProcessorWithInterceptors.class);
	
	private List<StanzaInterceptor> interceptors;

	public QueuedStanzaProcessorWithInterceptors(StanzaProcessor stanzaProcessor){
		this(stanzaProcessor,null);
	}
	
	public QueuedStanzaProcessorWithInterceptors(StanzaProcessor stanzaProcessor,
			List<StanzaInterceptor> interceptors){
		super(stanzaProcessor);
		this.interceptors=new ArrayList<StanzaInterceptor>();
		if(interceptors!=null){
			this.interceptors.addAll(interceptors);
		}
	}
	
	public void addIterceptor(StanzaInterceptor interceptors){
		this.interceptors.add(interceptors);
	}
	
	public void removeInterceptor(StanzaInterceptor interceptors){
		this.interceptors.remove(interceptors);
	}

	@Override
	public void processStanza(ServerRuntimeContext serverRuntimeContext,
			SessionContext sessionContext, Stanza stanza,
			SessionStateHolder sessionStateHolder){
		
		//check interceptors
		sessionContext.getInitiatingEntity();
		for(StanzaInterceptor interceptor:interceptors){
			if(interceptor.shouldIntercept(stanza)){
				if(interceptor.preProcess(stanza,sessionContext)==false){
					return;
				}
			}
		}
		
		super.processStanza(serverRuntimeContext,sessionContext,
				stanza,sessionStateHolder);
		
		/*
		 * 如果是单聊消息，给消息的发送方也返回服务器保存之后的消息
		 */
		if(ChatMessageStanzaInterceptor.isChatMessageStanza(stanza)){
			List<Attribute> replaceAttributes=Lists.newArrayList();
			replaceAttributes.add(new Attribute("to",stanza.getFrom().getBareJID().toString()));
			Stanza backStanza=StanzaBuilder.createClone(stanza,true,replaceAttributes).build();
			super.processStanza(serverRuntimeContext,sessionContext,
					backStanza,sessionStateHolder);
		}
	}
}
