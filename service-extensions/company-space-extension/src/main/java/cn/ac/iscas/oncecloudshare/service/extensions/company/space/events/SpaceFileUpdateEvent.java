package cn.ac.iscas.oncecloudshare.service.extensions.company.space.events;

import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.service.shiro.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class SpaceFileUpdateEvent extends SpaceFileEvent {
	private final Boolean oldFavorite;
	private final String oldDescription;
	
	public SpaceFileUpdateEvent(UserPrincipal principal, BaseSpace space, SpaceFile file) {
		super(principal, space, file, EVENT_UPDATE);
		this.oldFavorite = file.getFavorite();
		this.oldDescription = file.getDescription();
	}

	public Boolean getOldFavorite() {
		return oldFavorite;
	}

	public String getOldDescription() {
		return oldDescription;
	}
}
