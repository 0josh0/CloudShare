package cn.ac.iscas.oncecloudshare.service.service.authorization.principal;

import cn.ac.iscas.oncecloudshare.service.model.account.User;

public final class UserPrincipal implements InTenantPrincipal {

	private static final long serialVersionUID = -5674897217007175271L;

	public final Long tenantId;
	public final Long userId;
	public final String userName;

	public UserPrincipal(User user, Long tenantId) {

		this.tenantId = tenantId;

		this.userId = user.getId();
		this.userName = user.getName();
	}

	public Long getTenantId() {
		return tenantId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}
}
