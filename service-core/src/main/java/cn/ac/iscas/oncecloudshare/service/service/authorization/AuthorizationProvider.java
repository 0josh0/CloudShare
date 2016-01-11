package cn.ac.iscas.oncecloudshare.service.service.authorization;

import org.apache.shiro.authz.AuthorizationInfo;

import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;

public interface AuthorizationProvider {
	/**
	 * 通过principal获取用户的权限信息
	 * 
	 * @param principal
	 * @return
	 */
	public AuthorizationInfo getAuthorizationInfo(Principal principal);
	
	/**
	 * 获取缓存用户权限信息的key
	 * 
	 * @param principal
	 * @return
	 */
	public Object getAuthorizationCacheKey(Principal principal);
}