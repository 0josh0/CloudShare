package cn.ac.iscas.oncecloudshare.service.service.shiro.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import cn.ac.iscas.oncecloudshare.service.service.shiro.token.AnonToken;
import cn.ac.iscas.oncecloudshare.service.service.shiro.token.TicketToken;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.CloudShareTokenWrapper;

public class TicketOrAnonFilter extends TicketFilter {
	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		String ticket = getTicket(httpRequest);
		if (StringUtils.isEmpty(ticket)) {
			return createAnonToken();
		} else {
			return new CloudShareTokenWrapper(new TicketToken(ticket), getAuthorizationService());
		}
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		executeLogin(request, response);
		return true;
	}

	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		AuthenticationToken token = createToken(request, response);
		Subject subject = getSubject(request, response);
		try {
			subject.login(token);
			return onLoginSuccess(token, subject, request, response);
		} catch (AuthenticationException e) {
			subject.login(createAnonToken());
			return onLoginSuccess(token, subject, request, response);
		}
	}

	protected AuthenticationToken createAnonToken() {
		return new CloudShareTokenWrapper(AnonToken.of(), getAuthorizationService());
	}

	@Override
	protected Subject getSubject(ServletRequest request, ServletResponse response) {
		return super.getSubject(request, response);
	}
}
