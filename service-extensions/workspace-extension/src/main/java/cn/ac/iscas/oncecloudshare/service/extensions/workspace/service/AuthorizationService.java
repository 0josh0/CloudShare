package cn.ac.iscas.oncecloudshare.service.extensions.workspace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization.OperationStrategyFactory;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization.WorkspaceAuthorizationProvider;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Permissions;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthorizationProvider;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

@Service
public class AuthorizationService {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(AuthorizationService.class);

	@javax.annotation.Resource
	private OperationStrategyFactory operationStrategyFactory;
	@Resource
	private TenantService tenantService;
	@Resource
	private WorkspaceService workspaceService;
	// 用于缓存所有的权限提供者
	private Cache<String, AuthorizationProvider> authorizationProviders = CacheBuilder.newBuilder().softValues().build();

	@PostConstruct
	private void init() {
	}

	/**
	 * 创建工作空间的AuthorizationProvider
	 * 
	 * @param workspace
	 * @return
	 */
	public AuthorizationProvider createAuthorizationProvider(final Workspace workspace) {
		try {
			String key = tenantService.getCurrentTenant().getId() + "-" + workspace.getId();
			return authorizationProviders.get(key, new Callable<AuthorizationProvider>() {
				@Override
				public AuthorizationProvider call() throws Exception {
					WorkspaceAuthorizationProvider authorizationProvider = new WorkspaceAuthorizationProvider(workspace);
					((WorkspaceAuthorizationProvider) authorizationProvider).setWorkspaceService(workspaceService);
					((WorkspaceAuthorizationProvider) authorizationProvider).setAuthorizationService(AuthorizationService.this);
					return authorizationProvider;
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取角色对工作组某个成员的所有权限
	 * 
	 * @param role
	 * @param teamMate
	 * @return 如果没有权限，则返回的List中不会包括该权限
	 */
	public List<String> getPermissions(Long userId, String role, TeamMate teamMate) {
		List<String> permissions = new ArrayList<String>();
		// 群主和管理员有权限操作比自己等级低的人
		if (Roles.ADMIN.equals(role) || Roles.OWNER.equals(role)) {
			if (Roles.compare(role, teamMate.getRole()) > 0) {
				permissions.add("edit");
				permissions.add("changeRole");
				permissions.add("kick");
			}
		}
		if (teamMate.getUser().getId().equals(userId)) {
			permissions.add("edit");
			// 非群主可以退出空间
			if (!Roles.OWNER.equals(role)){
				permissions.add("kick");
			}
		}
		return permissions;
	}

	/**
	 * 获取角色对工作空间的权限
	 * 
	 * @param userId
	 * @param role
	 * @param workspace
	 * @return
	 */
	public List<String> getPermissions(Long userId, String role, Workspace workspace) {
		List<String> permissions = new ArrayList<String>();
		// 群主具有所有权限
		if (Roles.OWNER.equals(role)) {
			permissions.add("*");
		}
		// 管理员具有哪些权限呢
		else if (Roles.ADMIN.equals(role)) {
			permissions.add("*");
		}
		else {
			// 所有用户都有查看、下载、收藏权限
			permissions.add(Permissions.WorkSpace.VIEW);
			permissions.add(Permissions.WorkSpace.DOWNLOAD);
			permissions.add(Permissions.WorkSpace.FOLLOW);
			// writer
			if (Roles.WRITER.equals(role) || Roles.SEPARATED.equals(role)){
				permissions.add(Permissions.WorkSpace.EDIT);
				permissions.add(Permissions.WorkSpace.UPLOAD);
			} else if (Roles.LIMITED_WRITER.equals(role)){
				permissions.add(Permissions.WorkSpace.LIMITED_UPLOAD);
			}
			permissions = Lists.transform(permissions, Permissions.removeDomain);
		}
		return permissions;
	}

	public List<String> getChangedToRoles(String masterRole) {
		// 非管理无法更改角色
		if (Roles.compare(masterRole, Roles.ADMIN) < 0){
			return new ArrayList<String>(0);
		}
		int min = Roles.ALL.indexOf(Roles.READER);
		int start = Roles.ALL.indexOf(masterRole);
		List<String> results = Lists.newArrayList();
		for (int i = start + 1; i <= min; i++){
			results.add(Roles.ALL.get(i));
		}
		// 群主可以把管理员提升为群主
		if (Roles.OWNER.equals(masterRole)){
			results.add(Roles.OWNER);
		}
		
		return results;
	}
}
