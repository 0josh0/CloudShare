package cn.ac.iscas.oncecloudshare.service.service.shiro;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

public class UserBasicLoginExtension extends AbstractLoginExtension {
	@Override
	public String getName() {
		return "userbasic";
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