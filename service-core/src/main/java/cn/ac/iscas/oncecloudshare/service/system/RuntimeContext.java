package cn.ac.iscas.oncecloudshare.service.system;

import cn.ac.iscas.oncecloudshare.service.messaging.service.MessageService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.common.NotifService;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.EventBus;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtensionManager;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProviderRegistry;


@SuppressWarnings("deprecation")
public interface RuntimeContext {

	TenantService getTenantService();
	
	ServiceProviderRegistry getServiceProviderRegistry();
	
	AuthenticationService getAuthorizationService();
	
	ExtensionManager getExtensionManager();
	
	LoginExtensionManager getLoginExtensionManager();
	
	EventBus getEventBus();
	
	NotifService getNotifService();
	
	MessageService getMessageService();
	
	FileStorageService getFileStorageService();
	
	
}
