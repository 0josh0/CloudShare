package cn.ac.iscas.oncecloudshare.service.system.extension.login;

import org.apache.shiro.authc.AuthenticationToken;

import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;

@Deprecated
public class CloudShareTokenWrapper implements AuthenticationToken{
	private static final long serialVersionUID = -853947504847763410L;
	
	private AuthenticationToken authenticationToken;
	private AuthenticationService authorizationService;
	
	public CloudShareTokenWrapper(AuthenticationToken token, AuthenticationService authorizationService){
		this.authenticationToken = token;
		this.authorizationService = authorizationService;
	}
	
	public AuthenticationService getAuthorizationService(){
		return this.authorizationService;
	}
	
	public void setAuthorizationService(AuthenticationService authorizationService){
		this.authorizationService = authorizationService;
	}
	
	public AuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(AuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	@Override
	public Object getPrincipal() {
		return authorizationService.getPrincipal(authenticationToken);
	}

	@Override
	public Object getCredentials() {
		return authenticationToken.getCredentials();
	}
}