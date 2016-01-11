package cn.ac.iscas.oncecloudshare.service.service.shiro.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.shiro.token.TicketToken;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.CloudShareTokenWrapper;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

public class TicketFilter extends AuthenticatingFilter {
	private AuthenticationService authorizationService;

	private String headerTicket = "x-ticket";

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		String ticket = getTicket(httpRequest);
		TicketToken token=new TicketToken(ticket);
		return new CloudShareTokenWrapper(token, authorizationService);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		String ticket = getTicket(httpRequest);
		boolean isLogin = StringUtils.isNotEmpty(ticket) && executeLogin(request, response);
		if (!isLogin){
			RestException exception = new RestException(ErrorCode.INVALID_TICKET);
			String body=Gsons.defaultGson().toJson(exception.errorResponseDto);
			HttpServletResponse resp=WebUtils.toHttp(response);
			resp.setStatus(HttpStatus.UNAUTHORIZED.value());
			resp.getWriter().write(body);
			return false;
		}
		return isLogin;
	}	
	
	@Override
	protected Subject getSubject(ServletRequest request, ServletResponse response) {
		return super.getSubject(request, response);
	}

	protected String getTicket(HttpServletRequest httpRequest){
		String ticket = httpRequest.getHeader(headerTicket);
		if (StringUtils.isEmpty(ticket)) {
			return httpRequest.getParameter(headerTicket);
		}
		return ticket;
	}

	// ========================== getters and setters =================================

	public AuthenticationService getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthenticationService authorizationService) {
		this.authorizationService = authorizationService;
	}

	public String getParamTicket() {
		return headerTicket;
	}

	public void setParamTicket(String paramTicket) {
		this.headerTicket = paramTicket;
	}
}
