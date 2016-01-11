package cn.ac.iscas.oncecloudshare.service.service.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;

/**
 * 存储Principal
 *
 * @author Chen Hao
 */
public interface PrincipalService {

	/**
	 * 保存principal
	 * @param principal
	 * @param exipresIn 过期时间（毫秒）
	 * @return ticket
	 */
	@Nonnull String storePrincipal(@Nonnull Principal principal,long exipresIn,boolean updateOnTouch);
	
	/**
	 * 获取principal
	 * @param ticket
	 * @return
	 */
	@Nullable Principal getPrincipal(@Nonnull String ticket);
	
}
