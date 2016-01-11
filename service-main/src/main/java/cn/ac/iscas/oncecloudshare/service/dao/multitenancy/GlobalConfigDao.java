package cn.ac.iscas.oncecloudshare.service.dao.multitenancy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.GlobalConfig;

public interface GlobalConfigDao extends PagingAndSortingRepository<GlobalConfig, Long>, JpaSpecificationExecutor<GlobalConfig> {

	GlobalConfig findByKey(String key);

	List<GlobalConfig> findByKeyLike(String keyPattern);

	@Query("SELECT DISTINCT key FROM GlobalConfig")
	List<String> findAllKeys();
	
	
}
