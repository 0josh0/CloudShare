package cn.ac.iscas.oncecloudshare.service.service.shiro;

import javax.annotation.Resource;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import cn.ac.iscas.oncecloudshare.service.model.account.RoleEntry;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthorizationProvider;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.DownloadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UploadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.CloudShareTokenWrapper;

public class ShiroRealm extends AuthorizingRealm {
	@Resource
	private UserService userService;

	private ThreadLocal<AuthorizationProvider> authorizationProvider = new ThreadLocal<AuthorizationProvider>();

	public ShiroRealm() {
		super();
		setAuthenticationTokenClass(CloudShareTokenWrapper.class);
	}

	public void setAuthorizationProvider(AuthorizationProvider provider) {
		if (provider != null) {
			authorizationProvider.set(provider);
		} else {
			authorizationProvider.remove();
		}
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// 清除本地缓存的authorizationProvider
		authorizationProvider.remove();
		if (token == null) {
			return null;
		}
		Object principal = token.getPrincipal();
		if (principal != null) {
			return new SimpleAuthenticationInfo(principal, token.getCredentials(), getName());
		}
		return null;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Object principal = principals.getPrimaryPrincipal();
		if (authorizationProvider.get() == null) {
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			if (principal instanceof UserPrincipal) {
				info.addStringPermission("principal:user");
				UserPrincipal userPrincipal = (UserPrincipal) principal;
				User user = userService.find(userPrincipal.getUserId());
				if (user.getRoleEntries() != null || !user.getRoleEntries().isEmpty()) {
					for (RoleEntry roleEntry : user.getRoleEntries()) {
						info.addRole(roleEntry.toShiroRoleIdentifier());
					}
				}
			} else if (principal instanceof DownloadPrincipal) {
				info.addStringPermission("principal:download");
			} else if (principal instanceof UploadPrincipal) {
				info.addStringPermission("principal:upload");
			}
			return info;
		} else {
			return authorizationProvider.get().getAuthorizationInfo((Principal) principal);
		}
	}

	@Override
	protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
		if (authorizationProvider.get() == null) {
			return super.getAuthorizationCacheKey(principals);
		} else {
			Principal principal = (Principal) principals.getPrimaryPrincipal();
			return authorizationProvider.get().getAuthorizationCacheKey(principal);
		}
	}
}
