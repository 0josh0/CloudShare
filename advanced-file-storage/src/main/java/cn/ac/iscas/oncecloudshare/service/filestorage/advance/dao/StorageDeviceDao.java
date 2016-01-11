package cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;

public interface StorageDeviceDao extends
		PagingAndSortingRepository<StorageDevice, Long>,JpaSpecificationExecutor<StorageDevice> {

	/**
	 * 查找路径为uri的设备
	 * 
	 * @param uri
	 *            设备路径
	 * @return 路径为uri的设备对象
	 */
	StorageDevice findByDeviceUri(String uri);

	/**
	 * 查找状态为status的设备列表
	 * 
	 * @param status
	 *            设备状态
	 * @return 设备列表
	 */
	List<StorageDevice> findByStatus(StorageDevice.DeviceStatus status);

	/**
	 * 查找同时符合id和status 两个条件的设备
	 * 
	 * @param id
	 *            设备id
	 * @param status
	 *            设备状态
	 * @return 符合条件的设备对象
	 */
	StorageDevice findByIdAndStatus(Long id, StorageDevice.DeviceStatus status);

	@Query("update StorageDevice as device set device.status='ON' where device.status='ACTIVE'")
	@Modifying
	public void setActiveDeviceOn();
}
