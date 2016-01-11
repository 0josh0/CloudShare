package cn.ac.iscas.oncecloudshare.service.event.file;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class FileUntrashEvent extends FileEvent {
	private final File oldParent;
	private final String oldName;
	
	public FileUntrashEvent(UserPrincipal principal, File file) {
		super(principal, file, EVENT_UNTRASH);
		this.oldParent = file.getParent();
		this.oldName = file.getName();
	}

	public File getOldParent() {
		return oldParent;
	}

	public String getOldName() {
		return oldName;
	}

	public File getNewParent() {
		return getFile().getParent();
	}

	public String getNewName() {
		return getFile().getName();
	}
}
