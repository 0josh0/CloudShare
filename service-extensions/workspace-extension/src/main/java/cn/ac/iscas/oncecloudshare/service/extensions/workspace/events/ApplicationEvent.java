package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;

public class ApplicationEvent extends WorkspaceEvent {
	public static final int EVENT_CREATED = 1;
	public static final int EVENT_REVIEWED = EVENT_CREATED + 1;
	
	private final WorkspaceApplication application;
	
	public ApplicationEvent(UserPrincipal principal, Workspace workspace, int eventType, WorkspaceApplication application) {
		super(principal, workspace, eventType);
		this.application = application;
	}

	public WorkspaceApplication getApplication() {
		return application;
	}
}
