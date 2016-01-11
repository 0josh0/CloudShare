package cn.ac.iscas.oncecloudshare.service.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.TempItem;


public interface TempItemDao extends PagingAndSortingRepository<TempItem,Long>,
	JpaSpecificationExecutor<TempItem>{

	TempItem findByKey(String key);
	
	List<TempItem> findByType(String type);
	
	@Modifying
	@Query("DELETE FROM TempItem WHERE key=?1")
	int deleteByKey(String key);
	
	@Modifying
	@Query("DELETE FROM TempItem WHERE expiresAt<=?1")
	int deleteExpiredItem(long timeMillis);
}
