package cn.ac.iscas.oncecloudshare.service.system.extension.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.ac.iscas.oncecloudshare.service.system.extension.Extension;

@Deprecated
public interface LoginExtension extends Extension{
	/**
	 * 获取登录插件的名称
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * 生成token
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	CloudShareTokenWrapper createToken(HttpServletRequest request, HttpServletResponse response);
}