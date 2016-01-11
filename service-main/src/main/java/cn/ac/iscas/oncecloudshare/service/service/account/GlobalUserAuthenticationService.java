package cn.ac.iscas.oncecloudshare.service.service.account;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.GlobalUserService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;

@Service
public class GlobalUserAuthenticationService implements AuthenticationService{

	@Autowired
	TenantService tenantService;
	
	@Autowired
	GlobalUserService guService;
	
	@Autowired
	UserService userService;
	
	@PersistenceContext
	private EntityManager em;
	
	private Long getAndSetTenantId(String email){
		Long tenantId=guService.findTenantId(email);
		if(tenantId!=null){
			if(tenantService.setCurrentTenant(tenantId)){
				return tenantId;
			}
		}
		return null;
	}

	@Transactional
	@Override
	public Object getPrincipal(AuthenticationToken token){
		if(token==null ||
				! (token instanceof UsernamePasswordToken)){
			return null;
		}
		UsernamePasswordToken upToken=(UsernamePasswordToken)token;
		String email=upToken.getUsername();
		Long tenantId=getAndSetTenantId(email);
		if(tenantId==null){
			return null;
		}
		
		//手动切换schema
		String sql="USE "+Constants.TENANT_SCHEMA_PREFIX+tenantId;
		em.createNativeQuery(sql).executeUpdate();
		
		User user=userService.findByEmail(email);
		if(userService.verifyPassword(user,String.valueOf(upToken.getPassword()))){
			return new UserPrincipal(user,tenantId);
		}
		else{
			return null;
		}
	}

}
