package cn.ac.iscas.oncecloudshare.messaging.xmpp.server;

import java.util.List;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.delivery.StanzaRelay;
import org.apache.vysper.xmpp.protocol.HandlerDictionary;
import org.apache.vysper.xmpp.protocol.ProtocolWorker;
import org.apache.vysper.xmpp.protocol.StanzaProcessor;
import org.apache.vysper.xmpp.server.DefaultServerRuntimeContext;
import org.apache.vysper.xmpp.server.ServerFeatures;
import org.apache.vysper.xmpp.state.resourcebinding.ResourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.ChatMessageStanzaInterceptor;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.MucMessageStanzaInterceptor;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.NotifMessageStanzaInterceptor;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.QueuedStanzaProcessorWithInterceptors;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.protocol.StanzaInterceptor;

import com.google.common.collect.ImmutableList;


public class OcsServerRuntimeContext extends DefaultServerRuntimeContext {
	
	private static Logger logger=LoggerFactory.getLogger(OcsServerRuntimeContext.class);
	
	StanzaProcessor stanzaProcessor;

	public OcsServerRuntimeContext(Entity serverEntity,
			StanzaRelay stanzaRelay, ServerFeatures serverFeatures,
			List<HandlerDictionary> dictionaries,
			ResourceRegistry resourceRegistry){
		super(serverEntity, stanzaRelay, serverFeatures,
				dictionaries, resourceRegistry);
		
		/*
		 * 消息拦截器 
		 */
		List<StanzaInterceptor> interceptors=ImmutableList.
				<StanzaInterceptor>of(
					new NotifMessageStanzaInterceptor(),
					new ChatMessageStanzaInterceptor(),
					new MucMessageStanzaInterceptor());
		
		stanzaProcessor=new QueuedStanzaProcessorWithInterceptors(
				new ProtocolWorker(),interceptors);
	}

	@Override
	public StanzaProcessor getStanzaProcessor(){
		return stanzaProcessor;
	}
}
