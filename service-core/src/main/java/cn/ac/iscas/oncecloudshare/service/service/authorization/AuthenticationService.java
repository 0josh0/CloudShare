package cn.ac.iscas.oncecloudshare.service.service.authorization;

import org.apache.shiro.authc.AuthenticationToken;

import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;


public interface AuthenticationService extends ServiceProvider{

//	boolean verifyAccountExists(Object principal);
	
	Object getPrincipal(AuthenticationToken token);
}
