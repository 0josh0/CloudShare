package cn.ac.iscas.oncecloudshare.service.model.multitenancy;

public enum TenantStatus {
	REGISTERED, NORMAL, PRE_EXPIRED, EXPIRED;
	public static TenantStatus of(String value) {
		for (TenantStatus status : TenantStatus.values()) {
			if (status.name().equalsIgnoreCase(value))
				return status;
		}
		return null;
	}
}
