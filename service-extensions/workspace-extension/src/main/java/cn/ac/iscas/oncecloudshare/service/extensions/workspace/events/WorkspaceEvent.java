package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

@Interceptable
public class WorkspaceEvent extends UserRequestEvent {
	// 审核完成
	public static final int EVENT_REVIEWED = 1;

	private final Workspace workspace;
	private final int eventType;

	public WorkspaceEvent(UserPrincipal principal, Workspace workspace, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.workspace = workspace;
		this.eventType = eventType;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public int getEventType() {
		return eventType;
	}
}
