package cn.ac.iscas.oncecloudshare.service.event.file;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class FileRenameEvent extends FileEvent {
	private final String oldName;
	
	public FileRenameEvent(UserPrincipal principal, File file) {
		super(principal, file, EVENT_FILE_RENAME);
		this.oldName = file.getName();
	}

	public String getOldName() {
		return oldName;
	}

	public String getNewName() {
		return getFile().getName();
	}
}
