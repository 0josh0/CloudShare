package cn.ac.iscas.oncecloudshare.service.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.Config;


public interface ConfigDao extends PagingAndSortingRepository<Config,Long>,
	JpaSpecificationExecutor<Config>{

	Config findByKey(String key);
	
	List<Config> findByKeyLike(String keyPattern);
	
	@Query("SELECT DISTINCT key FROM Config")
	List<String> findAllKeys(); 
}