package cn.ac.iscas.oncecloudshare.messaging.service.authc;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.messaging.model.authc.UserInfo;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantTicket;
import cn.ac.iscas.oncecloudshare.messaging.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.messaging.service.shiro.TicketToken;

@Component(value="shiroRealm")
public class ShiroRealm extends AuthorizingRealm{
	
	@Autowired
	TenantService tenantService;
	
	@Autowired
	AccountService accountService;
	
	public ShiroRealm() {
		super();
		setAuthenticationTokenClass(TicketToken.class);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException{
		if(token instanceof TicketToken){
			TicketToken ticketToken=(TicketToken)token;
			TenantTicket ticket=new TenantTicket(
					tenantService.getCurrentTenant(),ticketToken.getTicket());
			UserInfo userInfo=accountService
					.getUserInfoByTicket(ticket);
			if(userInfo!=null){
				return new SimpleAuthenticationInfo(userInfo,
						ticketToken.getCredentials(),getName());
			}
		}
		return null;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals){
//		String username=principals.getPrimaryPrincipal().toString();
//		Entity entity=JIDUtil.tryParseEntity(username);
//		if(entity==null){
//			return null;
//		}
//		if(uService.verifyAccountExists(entity)){
//			return new SimpleAuthorizationInfo();
//		}
//		else{
//			return null;
//		}
		return null;
	}

}
