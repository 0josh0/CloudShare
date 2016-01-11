package cn.ac.iscas.oncecloudshare.service.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.TenantConfig;


public interface ConfigDao extends PagingAndSortingRepository<TenantConfig,Long>,
	JpaSpecificationExecutor<TenantConfig>{

	TenantConfig findByKey(String key);
	
	List<TenantConfig> findByKeyLike(String keyPattern);
	
	@Query("SELECT DISTINCT key FROM TenantConfig")
	List<String> findAllKeys(); 
}
