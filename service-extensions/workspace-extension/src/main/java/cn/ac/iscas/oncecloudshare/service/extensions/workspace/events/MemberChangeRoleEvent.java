package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class MemberChangeRoleEvent extends MemberEvent {
	private final String newRole;

	public MemberChangeRoleEvent(UserPrincipal principal, Workspace workspace, TeamMate member, String newRole) {
		super(principal, workspace, member, EVENT_CHANGE_ROLE);
		this.newRole = newRole;
	}

	public String getNewRole() {
		return newRole;
	}
}
