package cn.ac.iscas.oncecloudshare.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.messaging.model.iospush.IOSDevice;


public interface IOSDeviceDao extends PagingAndSortingRepository<IOSDevice,Long>{

	Page<IOSDevice> findByUserId(Long userId,Pageable pageable);

	IOSDevice findByUserIdAndDeviceToken(Long userId,String deviceToken);
	
	@Modifying
	@Query("DELETE FROM IOSDevice d WHERE d.userId=?1 AND d.deviceToken=?2")
	void deleteByUserIdAndDeviceToken(Long userId,String deviceToken);
}

