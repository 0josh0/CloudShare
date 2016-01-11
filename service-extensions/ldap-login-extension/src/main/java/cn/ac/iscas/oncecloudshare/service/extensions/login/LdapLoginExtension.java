package cn.ac.iscas.oncecloudshare.service.extensions.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import cn.ac.iscas.oncecloudshare.service.extensions.login.service.LdapUserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthorizationService;
import cn.ac.iscas.oncecloudshare.service.service.shiro.AbstractLoginExtension;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

public class LdapLoginExtension extends AbstractLoginExtension {
	public LdapLoginExtension() {
		this.setAuthorizationService((AuthorizationService) SpringUtil.getBean(LdapUserService.class));
	}

	@Override
	public String getName() {
		return "ldapbasic";
	}
	
	@Override
	protected AuthenticationToken createInternalToken(HttpServletRequest request, HttpServletResponse response) {
		String[] authInfo = getBasicPrincipalsAndCredentials(request);
		if (authInfo == null) {
			return new UsernamePasswordToken("", "", isRememberMe(request), getRequestHost(request));
		}
		return new UsernamePasswordToken(authInfo[0], authInfo[1], isRememberMe(request), getRequestHost(request));
	}
}