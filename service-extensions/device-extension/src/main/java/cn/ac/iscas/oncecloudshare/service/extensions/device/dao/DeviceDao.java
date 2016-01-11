package cn.ac.iscas.oncecloudshare.service.extensions.device.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.device.model.Device;

public interface DeviceDao extends PagingAndSortingRepository<Device, Long>, JpaSpecificationExecutor<Device> {
	@Query("from Device t1 where t1.mac = ?1")
	Device findByMac(String mac);
}