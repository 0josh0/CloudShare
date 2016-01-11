package cn.ac.iscas.oncecloudshare.service.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;


public class TenantIdResolver implements CurrentTenantIdentifierResolver{
	
	public TenantIdResolver(){
	}

	public String resolveCurrentTenantIdentifier(){
		TenantService tenantService=SpringUtil.getBean(TenantService.class);
		if(tenantService!=null){
			Tenant tenant=tenantService.getCurrentTenant();
			if(tenant!=null){
				return tenant.getId().toString();
			}
		}
		return "";
	}

	public boolean validateExistingCurrentSessions(){
		return true;
	}

}