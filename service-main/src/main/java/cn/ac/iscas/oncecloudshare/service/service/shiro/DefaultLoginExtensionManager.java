package cn.ac.iscas.oncecloudshare.service.service.shiro;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtension;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtensionManager;

import com.google.common.collect.Maps;

@Service("loginExtensionManager")
public class DefaultLoginExtensionManager implements LoginExtensionManager{
	
	@Autowired
	RuntimeContext runtimeContext;
	
	static final Logger _logger = LoggerFactory.getLogger(DefaultLoginExtensionManager.class);
	// 所有的登录插件
	private Map<String, LoginExtension> extensions = Maps.newConcurrentMap();
	// 默认的登录插件
	private LoginExtension defaultExtension;

	public void loadLoginExtension(String name,LoginExtension extension) {
		//可以直接配置文件里的name
		//可以使用ExtensionManager.isExtensionEnabled(name)判断extension是否启用
		extensions.put(name,extension);
	}

	public void unloadLoaginExtension(String name) {
		extensions.remove(name);
	}

	/**
	 * 通过名称获取对应的登录插件
	 * 
	 * @param name
	 * @return
	 */
	public LoginExtension getExtension(String name) {
		if (StringUtils.isEmpty(name)){
			return defaultExtension;
		}
		if(!runtimeContext.getExtensionManager().isExtensionEnabled(name)){
			return defaultExtension;
		}
		LoginExtension extension = extensions.get(name);
		return extension == null ? defaultExtension : extension;
	}

	// ================= getters and setters ===============

	public LoginExtension getDefaultExtension() {
		return defaultExtension;
	}

	public void setDefaultExtension(LoginExtension defaultExtension) {
		this.defaultExtension = defaultExtension;
	}
}