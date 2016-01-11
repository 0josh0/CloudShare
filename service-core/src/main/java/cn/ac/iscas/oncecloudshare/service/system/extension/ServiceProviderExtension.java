package cn.ac.iscas.oncecloudshare.service.system.extension;

import java.util.Set;

import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;


public interface ServiceProviderExtension extends Extension{

	Set<ServiceProvider> getServiceProviders();
}
