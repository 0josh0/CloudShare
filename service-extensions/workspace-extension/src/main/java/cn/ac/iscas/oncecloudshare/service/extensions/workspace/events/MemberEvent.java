package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import java.util.List;

import com.google.common.collect.ImmutableList;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

@Interceptable
public class MemberEvent extends UserRequestEvent {
	public static final int EVENT_JOINED = 1;
	public static final int EVENT_KICK = EVENT_JOINED + 1;
	public static final int EVENT_CHANGE_ROLE = EVENT_KICK + 1;

	private final Workspace workspace;
	private final List<TeamMate> members;
	private final int eventType;

	public MemberEvent(UserPrincipal principal, Workspace workspace, TeamMate member, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.workspace = workspace;
		this.members = ImmutableList.<TeamMate> of(member);
		this.eventType = eventType;
	}

	public MemberEvent(UserPrincipal principal, Workspace workspace, List<TeamMate> members, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.workspace = workspace;
		this.members = ImmutableList.<TeamMate> copyOf(members);
		this.eventType = eventType;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public List<TeamMate> getMembers() {
		return members;
	}

	public int getEventType() {
		return eventType;
	}
}
