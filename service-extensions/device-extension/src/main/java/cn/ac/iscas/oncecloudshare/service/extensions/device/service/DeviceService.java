package cn.ac.iscas.oncecloudshare.service.extensions.device.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dao.DeviceDao;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dao.DeviceLoginDao;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dao.DeviceUserDao;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dto.DeviceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.device.exceptions.DeviceDisabledException;
import cn.ac.iscas.oncecloudshare.service.extensions.device.exceptions.DeviceReviewRequiredException;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.Device;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceLogin;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceUser;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.utils.ip.IPSeeker;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

@Service
@Transactional(readOnly = true)
public class DeviceService {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DeviceService.class);
	@Resource
	private DeviceDao deviceDao;
	@Resource
	private DeviceUserDao deviceUserDao;
	@Resource
	private DeviceLoginDao deviceLoginDao;
	@Resource
	private ConfigService configService;
	@Resource
	private UserService userService;

	public Device getWebDevice() {
		String mac = Device.Type.web.toString();
		Device device = deviceDao.findByMac(mac);
		if (device == null) {
			device = new Device(Device.Type.web, mac);
			device = save(device);
		}
		return device;
	}

	public Device getAdminDevice() {
		String mac = Device.Type.web_admin.toString();
		Device device = deviceDao.findByMac(mac);
		if (device == null) {
			device = new Device(Device.Type.web_admin, mac);
			device = save(device);
		}
		return device;
	}

	public Device getUndefinedDevice() {
		String mac = Device.Type.undefined.toString();
		Device device = deviceDao.findByMac(mac);
		if (device == null) {
			device = new Device(Device.Type.undefined, mac);
			device = save(device);
		}
		return device;
	}

	public Device findByMac(String mac) {
		if (Device.Type.web.toString().equals(mac)) {
			return getWebDevice();
		} else if (Device.Type.web_admin.toString().equals(mac)) {
			return getAdminDevice();
		} else if (Device.Type.undefined.toString().equals(mac)) {
			return getUndefinedDevice();
		}
		return deviceDao.findByMac(mac);
	}

	@Transactional(readOnly = false)
	public Device save(Device device) {
		return deviceDao.save(device);
	}

	/**
	 * 设备是否需要审核
	 * 
	 * @return
	 */
	public boolean isReviewRequired() {
		return configService.getConfigAsBoolean(Configs.Keys.DEVICE_REVIEW_REQUIRED, Boolean.FALSE);
	}

	@Transactional(readOnly = false, noRollbackFor = { DeviceReviewRequiredException.class })
	public void userLogin(long userId, DeviceDto dto, String ip) {
		User user = userService.find(userId);
		// 判断Device
		Device device = findByMac(dto.mac);
		if (device == null) {
			device = new Device(dto.mac, null, Device.Type.valueOf(dto.type), dto.hardware, dto.osType, dto.osVersion);
			device = save(device);
		}
		// 判断DeviceUser
		DeviceUser deviceUser = deviceUserDao.findOne(device, user);
		if (deviceUser == null) {
			deviceUser = new DeviceUser(device, user);
			if (Device.Type.web.equals(device.getType()) && user.hasRole("sys", "admin")) {
				deviceUser.setStatus(DeviceUser.Status.ENABLE);
			}
			deviceUser = deviceUserDao.save(deviceUser);
		}
		if (DeviceUser.Status.DISABLED.equals(deviceUser.getStatus())) {
			throw new DeviceDisabledException();
		}
		if (isReviewRequired()) {
			if (DeviceUser.Status.CREATED.equals(deviceUser.getStatus())) {
				deviceUser.setStatus(DeviceUser.Status.APPLYING);
				deviceUser = deviceUserDao.save(deviceUser);
			}
			if (DeviceUser.Status.APPLYING.equals(deviceUser.getStatus())) {
				throw new DeviceReviewRequiredException();
			}
		}
		// 可以登录了
		DeviceLogin deviceLogin = new DeviceLogin();
		deviceLogin.setDeviceUser(deviceUser);
		deviceLogin.setIp(ip);
		deviceLogin.setLocation(IPSeeker.getInstance().getAddress(ip));
		deviceLogin.setLoginTime(new Date());
		deviceLoginDao.save(deviceLogin);

		deviceUserDao.updateLoginTimes(deviceUser.getId());
	}

	public Page<DeviceLogin> findLogins(List<SearchFilter> and, List<SearchFilter> or, Pageable pageable) {
		try {
			Specification<DeviceLogin> spec = Specifications.fromFilters(and, or, DeviceLogin.class);
			return deviceLoginDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public DeviceUser findDeviceUser(long id) {
		return deviceUserDao.findOne(id);
	}

	@Transactional(readOnly = false)
	public DeviceUser saveDeviceUser(DeviceUser deviceUser) {
		return deviceUserDao.save(deviceUser);
	}

	public Page<DeviceUser> findDeviceUsers(List<SearchFilter> and, List<SearchFilter> or, Pageable pageable) {
		try {
			Specification<DeviceUser> spec = Specifications.fromFilters(and, or, DeviceUser.class);
			return deviceUserDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public Page<Device> findDevices(List<SearchFilter> and, List<SearchFilter> or, Pageable pageable) {
		try {
			Specification<Device> spec = Specifications.fromFilters(and, or, Device.class);
			return deviceDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
}
