package cn.ac.iscas.oncecloudshare.service.service.shiro;

import javax.annotation.Resource;

import org.apache.shiro.authc.AuthenticationToken;

import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.AnonPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.InTenantPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;
import cn.ac.iscas.oncecloudshare.service.service.common.PrincipalService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.service.shiro.token.AnonToken;

public class TicketAuthorizationService implements AuthenticationService{
	
	@Resource
	private PrincipalService pService;
	
	@Resource
	private TenantService tenantService;
	

	@Override
	public Object getPrincipal(AuthenticationToken token) {
		if (token instanceof AnonToken){
			return AnonPrincipal.of();
		}
		Principal principal=pService.getPrincipal(token.getPrincipal().toString());
		if(principal instanceof InTenantPrincipal){
			Long tenantId=((InTenantPrincipal)principal).getTenantId();
			Long currentTenantId=tenantService.getCurrentTenant().getId();
			if(tenantId.equals(currentTenantId)==false){
				return null;
			}
		}
		return principal;
	}
}