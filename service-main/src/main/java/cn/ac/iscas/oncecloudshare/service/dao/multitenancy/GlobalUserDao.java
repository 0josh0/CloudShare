package cn.ac.iscas.oncecloudshare.service.dao.multitenancy;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.multitenancy.GlobalUser;


public interface GlobalUserDao extends PagingAndSortingRepository<GlobalUser,Long>{

	GlobalUser findByEmail(String email);
	
	@Modifying
	@Query("DELETE FROM GlobalUser gu WHERE gu.email=?1")
	int deleteByEmail(String email);
}
