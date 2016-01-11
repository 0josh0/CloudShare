package cn.ac.iscas.oncecloudshare.service.dto;

import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;

public class NotifTypes {
	public static enum Contact implements NotificationType{
		APPLY, REVIEW, DELETE;

		@Override
		public String getType() {
			return "CONTACT:".concat(name());
		}
	}
	
	public static enum Team implements NotificationType{
		JOINED, KICKED;

		@Override
		public String getType() {
			return "TEAM:".concat(name());
		}
	}
}
