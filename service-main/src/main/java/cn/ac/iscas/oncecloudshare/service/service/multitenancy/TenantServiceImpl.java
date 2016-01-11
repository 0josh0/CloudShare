package cn.ac.iscas.oncecloudshare.service.service.multitenancy;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.multitenancy.TennatDao;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.TenantStatus;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
public class TenantServiceImpl implements TenantService {

	@Autowired
	TennatDao tDao;
	
	ThreadLocal<Tenant> currentTenant=new ThreadLocal<Tenant>();	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public Tenant getCurrentTenant(){
		return currentTenant.get();
	}

	@Override
	@Transactional
	public boolean setCurrentTenant(Long id) {
		Tenant tenant=find(id); 
		if(tenant!=null && tenant.getStatus().equals(TenantStatus.NORMAL)){
			currentTenant.set(tenant);
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public boolean setCurrentTenantManually(long id) {
		Tenant tenant = find(id);
		if (tenant != null && tenant.getStatus().equals(TenantStatus.NORMAL)) {
			if (currentTenant.get() == null || !currentTenant.get().getId().equals(id)){
				String sql = "USE " + Constants.TENANT_SCHEMA_PREFIX + id;
				em.createNativeQuery(sql).executeUpdate();
				if (currentTenant.get() != null){
					// 清除缓存
					em.clear();
				}
			}
			currentTenant.set(tenant);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void clearCurrentTenant(){
		currentTenant.remove();
	}

	@Override
	public Tenant find(Long id){
		return tDao.findOne(id);
	}

	@Override
	public List<Tenant> findAll(List<SearchFilter> filters) {
		if (filters == null || filters.size() == 0){
			return Lists.newArrayList(tDao.findAll());
		}
		return tDao.findAll(Specifications.fromFilters(filters, Tenant.class));
	}
}
