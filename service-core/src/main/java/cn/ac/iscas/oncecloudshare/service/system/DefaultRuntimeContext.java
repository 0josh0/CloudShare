package cn.ac.iscas.oncecloudshare.service.system;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.messaging.service.MessageService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.common.NotifService;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.EventBus;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtensionManager;
import cn.ac.iscas.oncecloudshare.service.system.service.DefaultServiceProviderRegistry;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProviderRegistry;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

@SuppressWarnings("deprecation")
@Component
public class DefaultRuntimeContext implements RuntimeContext {

	ServiceProviderRegistry serviceProviderRegistry;
	FileStorageService fileStorageService;
	
	
	@Autowired
	TenantService tenantService;
	
	
	@PostConstruct
	void init(){
		serviceProviderRegistry=new DefaultServiceProviderRegistry();
//		serviceProviderRegistry.add(fileStorageService)
	}
	
	@Override
	public ServiceProviderRegistry getServiceProviderRegistry(){
		return serviceProviderRegistry;
	}
	
	@Override
	public AuthenticationService getAuthorizationService(){
		return retrieveServiceProviderWithDefaultSpringBeanClass(
				AuthenticationService.class,AuthenticationService.class); 
	}
	
	@Override
	public ExtensionManager getExtensionManager(){
		return SpringUtil.getBean(ExtensionManager.class);
	}
	
	@Override
	public LoginExtensionManager getLoginExtensionManager(){
		return SpringUtil.getBean(LoginExtensionManager.class);
	}
	
	@Override
	public EventBus getEventBus(){
		return getExtensionManager().getEventBus();
	}
	
	@Override
	public NotifService getNotifService(){
		return retrieveServiceProviderWithDefaultSpringBeanClass(
				NotifService.class,NotifService.class);
	}

	@Override
	public MessageService getMessageService() {
		return retrieveServiceProviderWithDefaultSpringBeanClass(MessageService.class, MessageService.class);
	}

	@Override
	public FileStorageService getFileStorageService(){
		return retrieveServiceProviderWithDefaultSpringBeanClass(
				FileStorageService.class,FileStorageService.class);
	}

	private <T> T retrieveServiceProviderWithDefaultSpringBeanClass(
			Class<? extends ServiceProvider> clazz,Class<? extends ServiceProvider> defaultSpringBeanClass){
		T t=serviceProviderRegistry.retrieve(clazz);
		if(t==null){
			return SpringUtil.getBean(defaultSpringBeanClass);
		}
		else{
			return t;
		}
	}

	@Override
	public TenantService getTenantService() {
		
		return tenantService;
	}

}
