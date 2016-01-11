package cn.ac.iscas.oncecloudshare.service.extensions.device.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceLogin;

public interface DeviceLoginDao extends PagingAndSortingRepository<DeviceLogin, Long>, JpaSpecificationExecutor<DeviceLogin> {
}