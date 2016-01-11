package cn.ac.iscas.oncecloudshare.service.service.shiro;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.CloudShareTokenWrapper;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtension;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

public abstract class AbstractLoginExtension implements LoginExtension {
	static final Logger _logger = LoggerFactory.getLogger(AbstractLoginExtension.class);

	// Basic认证的header名称
	private String headerBasicAuth = "Authorization";
	// 记住我参数名
	private String paramRememberMe = "rememberMe";

	private AuthenticationService authorizationService;

	public AuthenticationService getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthenticationService authorizationService) {
		this.authorizationService = authorizationService;
	}

	@Override
	public CloudShareTokenWrapper createToken(HttpServletRequest request, HttpServletResponse response) {
		return new CloudShareTokenWrapper(createInternalToken(request, response), getAuthorizationService());
	}

	protected abstract AuthenticationToken createInternalToken(HttpServletRequest request, HttpServletResponse response);

	protected String[] getBasicPrincipalsAndCredentials(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(headerBasicAuth);
		if (StringUtils.isEmpty(authorizationHeader)) {
			return null;
		}
		String[] authTokens = authorizationHeader.split(" ", 2);
		if (authTokens == null || authTokens.length < 2) {
			return null;
		}
		String decoded = Base64.decodeToString(authTokens[1]);
		String[] result = decoded.split(":", 2);
		if (result == null || result.length < 2) {
			return null;
		}
		return result;
	}

	protected String[] getFormPrincipalsAndCredentials(HttpServletRequest request) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		return new String[] { username, password };
	}

	protected String getRequestHost(HttpServletRequest request) {
		return request.getRemoteHost();
	}

	protected boolean isRememberMe(HttpServletRequest request) {
		String value = SpringUtil.getParamOrHeader(paramRememberMe);
		return value != null
				&& (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("enabled")
						|| value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on"));
	}
}
