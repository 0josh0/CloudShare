package cn.ac.iscas.oncecloudshare.service.event.file;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class FileUpdateEvent extends FileEvent {
	private final Boolean oldFavorite;
	private final String oldDescription;
	
	public FileUpdateEvent(UserPrincipal principal, File file) {
		super(principal, file, EVENT_UPDATE);
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
