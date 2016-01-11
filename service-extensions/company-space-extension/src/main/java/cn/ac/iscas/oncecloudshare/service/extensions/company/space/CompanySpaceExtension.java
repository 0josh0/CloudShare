package cn.ac.iscas.oncecloudshare.service.extensions.company.space;

import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.extensions.company.space.listeners.NotifSubscribers;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.service.CompanySpaceService;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;
import cn.ac.iscas.oncecloudshare.service.system.extension.ServiceProviderExtension;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;

import com.google.common.collect.ImmutableSet;

@Component
public class CompanySpaceExtension implements ListenerExtension, ServiceProviderExtension{
	@Resource
	private CompanySpaceService companySpaceService;
	
	@PostConstruct
	public void init(){
		companySpaceService.create();
	}
	
	@Override
	public Set<ServiceProvider> getServiceProviders() {
		return Collections.emptySet();
	}
	
	@Override
	public Set<Object> getListeners() {
		return Collections.emptySet();
//		return ImmutableSet.<Object>of(NotifSubscribers.getInstance());
	}
}
