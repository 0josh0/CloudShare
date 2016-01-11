package cn.ac.iscas.oncecloudshare.messaging.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import cn.ac.iscas.oncecloudshare.messaging.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.messaging.utils.SpringUtil;


public class TenantIdResolver implements CurrentTenantIdentifierResolver{
	
	public TenantIdResolver(){
	}

	public String resolveCurrentTenantIdentifier(){
		TenantService tenantService=SpringUtil.getBean(TenantService.class);
		if(tenantService!=null){
			Long tenant=tenantService.getCurrentTenant();
			if(tenant!=null){
				return tenant.toString();
			}
		}
		return "";
	}

	public boolean validateExistingCurrentSessions(){
		return true;
	}

}