package cn.ac.iscas.oncecloudshare.service.event;

import javax.servlet.http.HttpServletRequest;

import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;

public class VersionEvent  extends UserRequestEvent{

	public VersionEvent(HttpServletRequest request, UserPrincipal principal) {
		super(request, principal);
		// TODO Auto-generated constructor stub
	}

}
