package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class WorkspaceReviewEvent extends WorkspaceEvent {
	private final boolean agreed;

	public WorkspaceReviewEvent(UserPrincipal principal, Workspace workspace, boolean agreed) {
		super(principal, workspace, WorkspaceEvent.EVENT_REVIEWED);
		this.agreed = agreed;
	}

	public boolean isAgreed() {
		return agreed;
	}
}
