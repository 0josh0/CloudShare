package cn.ac.iscas.oncecloudshare.messaging.xmpp.storage;

import javax.annotation.PostConstruct;

import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.messaging.service.authc.AccountService;
import cn.ac.iscas.oncecloudshare.messaging.service.iospush.IOSPusher;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.RoomService;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.account.OcsRosterManager;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.account.OcsUserAuthorization;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.storage.OcsOccupantStorageProvider;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.storage.OcsRoomStorageProvider;

@Component(value="ocsStorageProviderRegistry")
public class OcsStorageProviderRegistry extends OpenStorageProviderRegistry {
	
	@Autowired
	IOSPusher iosPusher;
	
	@Autowired
	RoomService rService;
	
	@Autowired
	OccupantService oService;
	
	@Autowired
	AccountService aService;
	
	public OcsStorageProviderRegistry(){
		
	}
	
	@PostConstruct
	public void init(){
		add(new OcsUserAuthorization());
        add(new OcsRosterManager());

        // provider from external modules, low coupling, fail when modules are not present
//        add(new LeafNodeInMemoryStorageProvider());
//        add(new CollectionNodeInMemoryStorageProvider());
        
        add(new OcsOfflineStorageProvider(aService,iosPusher));
       
        OcsOccupantStorageProvider occupantProvider=new OcsOccupantStorageProvider(oService);
        add(occupantProvider);
        add(new OcsRoomStorageProvider(occupantProvider,rService,oService));
	}
}
