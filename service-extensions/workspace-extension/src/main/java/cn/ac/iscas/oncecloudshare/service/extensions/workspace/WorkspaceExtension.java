package cn.ac.iscas.oncecloudshare.service.extensions.workspace;

import java.util.Collections;
import java.util.Set;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners.NotifSubscribers;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners.WDeleteIndexListener;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners.WUpdateIndexListener;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;
import cn.ac.iscas.oncecloudshare.service.system.extension.ServiceProviderExtension;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.collect.ImmutableSet;

public class WorkspaceExtension implements ListenerExtension, ServiceProviderExtension{	
	@Override
	public Set<ServiceProvider> getServiceProviders() {
		return Collections.emptySet();
	}
	
	@Override
	public Set<Object> getListeners() {
		return ImmutableSet.<Object> builder().add(SpringUtil.getBean(WUpdateIndexListener.class)).add(NotifSubscribers.getInstance())
		.add(SpringUtil.getBean(WDeleteIndexListener.class)).build();
	}
}
