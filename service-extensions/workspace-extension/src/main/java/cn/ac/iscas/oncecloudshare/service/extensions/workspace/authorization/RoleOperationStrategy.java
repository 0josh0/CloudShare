package cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Permissions;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;

public class RoleOperationStrategy extends OperationStrategyHandler {
	public RoleOperationStrategy() {
	}

	@Override
	public String getName() {
		return "role";
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public boolean isPermit(Long userId, String role, String operation) {
		// >=reader才有下载的权限
		if (Permissions.WorkSpace.DOWNLOAD.equals(operation)) {
			return Roles.compare(role, Roles.READER) >= 0;
		}
		// >=writer才有上传的权限
		else if (Permissions.WorkSpace.UPLOAD.equals(operation)) {
			return Roles.compare(role, Roles.WRITER) >= 0;
		}
		// >=reader才有收藏的权限
		else if (Permissions.WorkSpace.FOLLOW.equals(operation)) {
			return Roles.compare(role, Roles.READER) >= 0;
		}
		return false;
	}
}
