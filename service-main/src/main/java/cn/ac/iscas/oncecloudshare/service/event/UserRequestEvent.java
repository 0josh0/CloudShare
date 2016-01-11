package cn.ac.iscas.oncecloudshare.service.event;

import javax.servlet.http.HttpServletRequest;

import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;


public class UserRequestEvent extends RequestEvent{

	UserPrincipal principal;
	
	public UserRequestEvent(HttpServletRequest request,
			UserPrincipal principal){
		super(request);
		this.principal=principal;
	}
	
	public UserPrincipal getPrincipal(){
		return principal;
	}
}
