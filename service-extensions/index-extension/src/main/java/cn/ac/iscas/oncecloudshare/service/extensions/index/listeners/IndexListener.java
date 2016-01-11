package cn.ac.iscas.oncecloudshare.service.extensions.index.listeners;

import javax.annotation.Resource;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.index.service.IndexNotifyService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;

public abstract class  IndexListener<T extends UserRequestEvent> {
	
	@Resource
	protected IndexNotifyService notifyService;
	
	@Resource
	protected TenantService tenantService;

	public abstract void handleEvent(T event);

}
