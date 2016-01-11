package cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization;

import java.util.List;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.AuthorizationService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.WorkspaceService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthorizationProvider;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;

public class WorkspaceAuthorizationProvider implements AuthorizationProvider {
	private Workspace workspace;
	private WorkspaceService workspaceService;
	private AuthorizationService authorizationService;

	public WorkspaceAuthorizationProvider(Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public AuthorizationInfo getAuthorizationInfo(Principal principal) {
		Long userId = getRequestUserId(principal);
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		// 判断worksapce状态
		String status = workspace.getStatus();
		if (status.equals(WorkspaceUtils.Status.APPLY)) {
			// 只有申请者可以做查看和取消申请操作
			if (workspace.getApplyBy().getId().equals(userId)) {
				info.addStringPermission("workspace:view");
				info.addStringPermission("workspace:cancelApply");
			}
		} else if (status.equals(WorkspaceUtils.Status.ACITVE)) {
			String role = workspaceService.getUserRole(workspace, userId);
			List<String> permissions = authorizationService.getPermissions(userId, role, workspace);
			for (String permission : permissions){
				info.addStringPermission("workspace:" + permission);
			}
			info.addRole(role);
		} else if (status.equals(WorkspaceUtils.Status.REFUSED)) {
			// 只有申请者可以做查看和取消申请操作
			if (workspace.getApplyBy().getId().equals(userId)) {
				info.addStringPermission("workspace:view");
				info.addStringPermission("workspace:cancelApply");
			}
		} else if (status.equals(WorkspaceUtils.Status.CANCELED)) {
			// 只有申请者可以做查看操作
			if (workspace.getApplyBy().getId().equals(userId)) {
				info.addStringPermission("workspace:view");
			}
		}
		return info;
	}

	private Long getRequestUserId(Principal principal) {
		if (principal instanceof UserPrincipal) {
			return ((UserPrincipal) principal).getUserId();
		}
		return null;
	}
	
	@Override
	public Object getAuthorizationCacheKey(Principal principal) {
		UserPrincipal userPrincipal = (UserPrincipal) principal;
		return userPrincipal.getTenantId() + "-" + userPrincipal.getUserId() + "." + WorkspaceUtils.DOMAIN + "." + workspace.getId();
	}

	// ==================== getters and setters ============================

	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public void setWorkspaceService(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public AuthorizationService getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
	}
}
