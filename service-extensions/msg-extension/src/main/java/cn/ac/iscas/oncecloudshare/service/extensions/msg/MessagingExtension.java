package cn.ac.iscas.oncecloudshare.service.extensions.msg;

import java.util.Set;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.extensions.msg.service.JmsNotifService;
import cn.ac.iscas.oncecloudshare.service.system.extension.ServiceProviderExtension;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.collect.ImmutableSet;

@Component
public class MessagingExtension implements ServiceProviderExtension{

	@Override
	public Set<ServiceProvider> getServiceProviders(){
		return ImmutableSet.<ServiceProvider>builder()
				.add(SpringUtil.<JmsNotifService>getBean(JmsNotifService.class))
				.build();
	}

}
