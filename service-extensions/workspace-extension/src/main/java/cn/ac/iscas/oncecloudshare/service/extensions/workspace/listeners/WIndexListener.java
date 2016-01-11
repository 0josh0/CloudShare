package cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners;

import javax.annotation.Resource;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.WIndexNotifyService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;

public abstract class  WIndexListener<T extends UserRequestEvent> {
	
	@Resource
	protected WIndexNotifyService notifyService;
	@Resource
	protected TenantService tenantService;

	public abstract void handleEvent(T event);

}
