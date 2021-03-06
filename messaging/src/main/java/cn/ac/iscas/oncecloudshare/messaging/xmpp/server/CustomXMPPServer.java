package cn.ac.iscas.oncecloudshare.messaging.xmpp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.authorization.Plain;
import org.apache.vysper.xmpp.authorization.SASLMechanism;
import org.apache.vysper.xmpp.cryptography.BogusTrustManagerFactory;
import org.apache.vysper.xmpp.cryptography.InputStreamBasedTLSContextFactory;
import org.apache.vysper.xmpp.delivery.OfflineStanzaReceiver;
import org.apache.vysper.xmpp.delivery.StanzaRelayBroker;
import org.apache.vysper.xmpp.delivery.inbound.DeliveringExternalInboundStanzaRelay;
import org.apache.vysper.xmpp.modules.Module;
import org.apache.vysper.xmpp.modules.extension.xep0160_offline_storage.OfflineStorageProvider;
import org.apache.vysper.xmpp.modules.roster.RosterModule;
import org.apache.vysper.xmpp.modules.servicediscovery.ServiceDiscoveryModule;
import org.apache.vysper.xmpp.protocol.HandlerDictionary;
import org.apache.vysper.xmpp.server.DefaultServerRuntimeContext;
import org.apache.vysper.xmpp.server.Endpoint;
import org.apache.vysper.xmpp.server.ServerFeatures;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.state.resourcebinding.ResourceRegistry;

import cn.ac.iscas.oncecloudshare.messaging.xmpp.delivery.OcsDeliveringInternalInboundStanzaRelay;

/**
 * 修改 {@link org.apache.vysper.xmpp.server.XMPPServer} 里的
 * ServerRuntimeContext和internalStanzaRelay
 * 
 * @author Chen Hao
 *
 */
class CustomXMPPServer {

    private final List<SASLMechanism> saslMechanisms = new ArrayList<SASLMechanism>();

    private String serverDomain;

    private DefaultServerRuntimeContext serverRuntimeContext;

    private StorageProviderRegistry storageProviderRegistry;

    private InputStream tlsCertificate;

    private String tlsCertificatePassword;

    private final List<Endpoint> endpoints = new ArrayList<Endpoint>();
    

    public CustomXMPPServer(String domain) {
        this.serverDomain = domain;

        // default list of SASL mechanisms
        saslMechanisms.add(new Plain());
    }

    public void setSASLMechanisms(List<SASLMechanism> validMechanisms) {
        saslMechanisms.addAll(validMechanisms);
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry) {
        this.storageProviderRegistry = storageProviderRegistry;
    }

    public void setTLSCertificateInfo(File certificate, String password) throws FileNotFoundException {
        tlsCertificate = new FileInputStream(certificate);
        tlsCertificatePassword = password;
    }

    public void setTLSCertificateInfo(InputStream certificate, String password) {
        tlsCertificate = certificate;
        tlsCertificatePassword = password;
    }

    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    public void start() throws Exception {

        BogusTrustManagerFactory bogusTrustManagerFactory = new BogusTrustManagerFactory();
        InputStreamBasedTLSContextFactory tlsContextFactory = new InputStreamBasedTLSContextFactory(tlsCertificate);
        tlsContextFactory.setPassword(tlsCertificatePassword);
        tlsContextFactory.setTrustManagerFactory(bogusTrustManagerFactory);

        List<HandlerDictionary> dictionaries = new ArrayList<HandlerDictionary>();
        addCoreDictionaries(dictionaries);

        ResourceRegistry resourceRegistry = new ResourceRegistry();

        EntityImpl serverEntity = new EntityImpl(null, serverDomain, null);

        AccountManagement accountManagement = (AccountManagement) storageProviderRegistry
                .retrieve(AccountManagement.class);
        OfflineStanzaReceiver offlineReceiver = (OfflineStanzaReceiver) storageProviderRegistry.retrieve(OfflineStorageProvider.class);
        
        /*
         * 修改internalStanzaRelay
         */
//        DeliveringInternalInboundStanzaRelay internalStanzaRelay = new DeliveringInternalInboundStanzaRelay(serverEntity,
//                resourceRegistry, accountManagement,offlineReceiver);
        OcsDeliveringInternalInboundStanzaRelay internalStanzaRelay = new OcsDeliveringInternalInboundStanzaRelay(
        		serverEntity,resourceRegistry, accountManagement,offlineReceiver);
        DeliveringExternalInboundStanzaRelay externalStanzaRelay = new DeliveringExternalInboundStanzaRelay();

        StanzaRelayBroker stanzaRelayBroker = new StanzaRelayBroker();
        stanzaRelayBroker.setInternalRelay(internalStanzaRelay);
        stanzaRelayBroker.setExternalRelay(externalStanzaRelay);

        ServerFeatures serverFeatures = new ServerFeatures();
        serverFeatures.setAuthenticationMethods(saslMechanisms);

        //修改ServerRuntimeContext
        serverRuntimeContext = new OcsServerRuntimeContext(serverEntity, stanzaRelayBroker, serverFeatures,
                dictionaries, resourceRegistry);
        serverRuntimeContext.setStorageProviderRegistry(storageProviderRegistry);
        serverRuntimeContext.setTlsContextFactory(tlsContextFactory);

        serverRuntimeContext.addModule(new ServiceDiscoveryModule());
        serverRuntimeContext.addModule(new RosterModule());

        stanzaRelayBroker.setServerRuntimeContext(serverRuntimeContext);
        internalStanzaRelay.setServerRuntimeContext(serverRuntimeContext);
        externalStanzaRelay.setServerRuntimeContext(serverRuntimeContext);

        if (endpoints.size() == 0)
            throw new IllegalStateException("server must have at least one endpoint");
        for (Endpoint endpoint : endpoints) {
            endpoint.setServerRuntimeContext(serverRuntimeContext);
            endpoint.start();
        }
    }

    public void stop() {
        for (Endpoint endpoint : endpoints) {
            endpoint.stop();
        }
        
        serverRuntimeContext.getServerConnectorRegistry().close();
    }

    public void addModule(Module module) {
        serverRuntimeContext.addModule(module);
    }

    private void addCoreDictionaries(List<HandlerDictionary> dictionaries) {
        dictionaries.add(new org.apache.vysper.xmpp.modules.core.base.BaseStreamStanzaDictionary());
        dictionaries.add(new org.apache.vysper.xmpp.modules.core.starttls.StartTLSStanzaDictionary());
        dictionaries.add(new org.apache.vysper.xmpp.modules.core.sasl.SASLStanzaDictionary());
        dictionaries.add(new org.apache.vysper.xmpp.modules.core.bind.BindResourceDictionary());
        dictionaries.add(new org.apache.vysper.xmpp.modules.core.session.SessionStanzaDictionary());
        dictionaries.add(new org.apache.vysper.xmpp.modules.core.compatibility.jabber_iq_auth.JabberIQAuthDictionary());
    }
    
    public ServerRuntimeContext getServerRuntimeContext() {
        return serverRuntimeContext;
    }
}
