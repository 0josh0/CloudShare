package cn.ac.iscas.oncecloudshare.messaging.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.utils.Constants;

public class BaseService {
	
	private static final Logger logger=LoggerFactory.getLogger(BaseService.class);

	@PersistenceContext
	private EntityManager em;
	
	protected boolean changeTenantSchema(long tenantId){
		if(checkTenantSchemaExists(tenantId)==false){
			return false;
		}
		try{
			String sql="USE "+Constants.TENANT_SCHEMA_PREFIX+tenantId;
			em.createNativeQuery(sql).executeUpdate();
			return true;
		}
		catch(Exception e){
			logger.error("error checking tenant exists",e);
			return false;
		}
	}
	
	protected boolean checkTenantSchemaExists(long tenantId){
		try{
			String sql="SHOW SCHEMAS LIKE '"
					+Constants.TENANT_SCHEMA_PREFIX+tenantId+"'";
			@SuppressWarnings ("rawtypes")
			List res=em.createNativeQuery(sql).getResultList();
			return res.isEmpty()==false;
		}
		catch (Exception e) {
			logger.error("error checking tenant exists",e);
			return false;
		}
	}
}
