package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;

public enum WorkspaceNotifType implements NotificationType {
	MEMBER_JOINED, 
	MEMBER_KICKED, 
	// 申请被审核
	APP_REVIEWED,
	// 申请被创建
	APP_CREATED,
	ROLE_CHANGED, CLEAR_TRASH, FILE_DELETED, FOLDER_CREATED, FILE_MOVED, FILE_TRASHED, FILE_UNTRASHED, FILE_UPDATED, FILE_UPLOADED, FILE_DOWNLOADED;

	@Override
	public String getType() {
		return "EXTS.WORKSPACE:" + name();
	}
}
