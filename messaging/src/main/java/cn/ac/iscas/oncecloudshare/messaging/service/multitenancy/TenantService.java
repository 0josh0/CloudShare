package cn.ac.iscas.oncecloudshare.messaging.service.multitenancy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;

@Service
public class TenantService extends BaseService{

	private ThreadLocal<Long> currentTenant=new ThreadLocal<Long>();

	public Long getCurrentTenant(){
		return currentTenant.get();
	}

	@Transactional
	public boolean setCurrentTenant(long tenantId){
		if(!changeTenantSchema(tenantId)){
			return false;
		}
		currentTenant.set(tenantId);
		return true;
	}
	
	public boolean verifyTenantExist(long tenantId){
		return checkTenantSchemaExists(tenantId);
	}
}
