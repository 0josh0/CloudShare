package cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;

public class UserOperationStrategy extends OperationStrategyHandler {
	@Override
	public String getName() {
		return "user";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean isPermit(Long userId, String role, String operation) {
		return Roles.compare(role, Roles.USER) >= 0;
	}
}
