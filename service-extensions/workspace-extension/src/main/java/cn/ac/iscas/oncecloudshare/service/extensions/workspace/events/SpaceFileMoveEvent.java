package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class SpaceFileMoveEvent extends SpaceFileEvent {
	private final SpaceFile oldParent;
	private final String oldName;
	
	public SpaceFileMoveEvent(UserPrincipal principal, Workspace workspace, SpaceFile file) {
		super(principal, workspace, file, EVENT_MOVE);
		this.oldParent = file.getParent();
		this.oldName = file.getName();
	}

	public SpaceFile getOldParent() {
		return oldParent;
	}

	public String getOldName() {
		return oldName;
	}

	public SpaceFile getNewParent() {
		return getFile().getParent();
	}

	public String getNewName() {
		return getFile().getName();
	}
}
