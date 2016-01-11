package cn.ac.iscas.oncecloudshare.service.event.login;

import javax.servlet.http.HttpServletRequest;

import cn.ac.iscas.oncecloudshare.service.event.RequestEvent;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

@Interceptable
public class LoginEvent extends RequestEvent {

	private final UserPrincipal principal;
	// 登录凭证
	private final String ticket;

	public LoginEvent(HttpServletRequest request, UserPrincipal principal, String ticket) {
		super(request);
		this.principal = principal;
		this.ticket = ticket;
	}

	public UserPrincipal getPrincipal() {
		return principal;
	}

	public String getTicket() {
		return ticket;
	}
}
