package cn.ac.iscas.oncecloudshare.service.dto;

import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;

public enum NotifType implements NotificationType {
	SHARE_CREATE, SHARE_CANCEL,COMMENT_AT;

	@Override
	public String getType() {
		return name();
	}
}