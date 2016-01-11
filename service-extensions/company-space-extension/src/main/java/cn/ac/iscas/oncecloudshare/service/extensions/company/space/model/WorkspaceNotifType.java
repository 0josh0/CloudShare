package cn.ac.iscas.oncecloudshare.service.extensions.company.space.model;

import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;

public enum WorkspaceNotifType implements NotificationType {
	MEMBER_JOINED, MEMBER_KICKED, APP_REVIEWED, ROLE_CHANGED, CLEAR_TRASH, FILE_DELETED, FOLDER_CREATED, FILE_MOVED, FILE_TRASHED, FILE_UNTRASHED, FILE_UPDATED, FILE_UPLOADED, FILE_DOWNLOADED;

	@Override
	public String getType() {
		return "EXTS.WORKSPACE:" + name();
	}
}
