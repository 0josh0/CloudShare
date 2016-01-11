package cn.ac.iscas.oncecloudshare.service.model.account;

public enum UserStatus {
	ACTIVE, // 正常用户
	FROZEN, // 冻结用户
	APPLYING, // 正在申请
	DELETED, // 已删除
	/**
	 * 已邀请，尚未激活
	 */
	UNACTIVATED,
	// DEV_DISACTIVE,//设备禁用
	// DEV_REJECT,//设备审核驳回
	// DEV_APPLY//设备等待审核
	;

	public static UserStatus of(String identifier) {
		for (UserStatus value : values()) {
			if (value.name().equalsIgnoreCase(identifier)) {
				return value;
			}
		}
		return null;
	}
}
