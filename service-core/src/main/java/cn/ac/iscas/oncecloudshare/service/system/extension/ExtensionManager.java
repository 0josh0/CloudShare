package cn.ac.iscas.oncecloudshare.service.system.extension;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.EventBus;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtension;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;
import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;


@Service
public class ExtensionManager {
	
	private static Logger logger=LoggerFactory.getLogger(ExtensionManager.class);
	
	public static final String EXT_NAME_REGEX="([a-zA-Z0-9_]){6,32}";
	
	@Autowired
	RuntimeContext runtimeContext;
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	
	public static class ExtensionHolder{
		public String name;
		public String version;
		public String description;
		public boolean enabled;
		@GsonHidden
		Extension extension;
	}
	
	Map<String,ExtensionHolder> extMap=Maps.newConcurrentMap();
	
	EventBus eventBus;
	
	public ExtensionManager(){
		eventBus=new EventBus(this);
	}
	
	public EventBus getEventBus(){
		return eventBus;
	}
	
	public void loadExtension(String name,String version,
			String description,Extension extension){
		if(Pattern.matches(EXT_NAME_REGEX,name)==false ||
				Strings.isNullOrEmpty(version)){
			logger.warn("invalid extension properties");
			return;
		}
		ExtensionHolder holder=extMap.get(name);
		if(holder!=null){
			logger.warn("extension name [ "+name+" ] already exists with class: "+
					extension.getClass().getCanonicalName());
			return;
		}
		holder=new ExtensionHolder();
		holder.name=name;
		holder.version=version;
		holder.description=description;
		holder.enabled=readExtEnabledConfig(name);
		holder.extension=extension;
		extMap.put(name,holder);
		
		//register listeners
		if(extension instanceof ListenerExtension){
			for(Object listener:((ListenerExtension)extension).getListeners()){
				eventBus.register(name,listener);
			}
		}
		
		//register ServiceProvider
		if(extension instanceof ServiceProviderExtension){
			for(ServiceProvider sp:
				((ServiceProviderExtension)extension).getServiceProviders()){
				runtimeContext.getServiceProviderRegistry().add(sp);
			}
		}
		
		//register LoginExtension
		if(extension instanceof LoginExtension){
			runtimeContext.getLoginExtensionManager()
				.loadLoginExtension(name,(LoginExtension)extension);
		}
		
		logger.info("extension loaded: "+name+"-"+version);
	}
	
	private boolean readExtEnabledConfig(String extName){
		return configService.getConfigAsBoolean(
				Configs.Keys.extensionEnabled(extName),true);
	}
	
	private void setExtEnabledConfig(String extName,boolean enabled){
		configService.saveConfig(Configs.Keys.extensionEnabled(extName),enabled,true);
	}
	
	/**
	 * 结束时把enabled配置保存的数据库
	 */
	@PreDestroy
	protected void preDestroy(){
		for(ExtensionHolder holder:extMap.values()){
			if(holder.enabled==false){
				setExtEnabledConfig(holder.name,false);
			}
		}
	}
	
	public ExtensionHolder find(String extName){
		return extMap.get(extName);
	}
	
	public Collection<ExtensionHolder> findAll(){
		return extMap.values();
	}
	
	public void setExtensionEnabled(String extName,boolean enabled){
		ExtensionHolder holder=extMap.get(extName);
		if(holder!=null){
			holder.enabled=enabled;
		}
	}

	public boolean isExtensionEnabled(String extName){
		ExtensionHolder holder=extMap.get(extName);
		return holder!=null?holder.enabled:false;
	}
}
