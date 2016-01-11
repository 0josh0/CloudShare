package cn.ac.iscas.oncecloudshare.service.extensions.device.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.device.model.Device;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceUser;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

public interface DeviceUserDao extends PagingAndSortingRepository<DeviceUser, Long>, JpaSpecificationExecutor<DeviceUser> {
	@Query("from DeviceUser t1 where t1.device = ?1 and t1.user = ?2")
	public DeviceUser findOne(Device device, User user);
	
	@Modifying
	@Query("update DeviceUser t1 set t1.loginTimes = t1.loginTimes + 1 where id = ?1")
	public void updateLoginTimes(long id);
}