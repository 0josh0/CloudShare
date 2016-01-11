package cn.ac.iscas.oncecloudshare.service.extensions.company.space.events;

import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.service.shiro.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class SpaceFileUntrashEvent extends SpaceFileEvent {
	private final SpaceFile oldParent;
	private final String oldName;
	
	public SpaceFileUntrashEvent(UserPrincipal principal, BaseSpace space, SpaceFile file) {
		super(principal, space, file, EVENT_UNTRASH);
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
