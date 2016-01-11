package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao.StorageDeviceDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.exceptions.DeviceNotFoundException;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.exceptions.DuplicateDeviceException;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Strings;

@Service
@Transactional
public class StorageDeviceService {
	private static final boolean WINDOWS = System.getProperty("os.name")
			.toLowerCase().startsWith("windows");

	@Autowired
	private StorageDeviceDao deviceDao;

	/**
	 * 添加一个新的存储设备（磁盘目录）
	 * 
	 * @param deviceUri
	 *            存储设备的路径
	 * @return 成功则返回新添加的StorageDevice 对象
	 */
	public StorageDevice addDevice(String deviceUri) {
		if (Strings.isNullOrEmpty(deviceUri)) {
			throw new IllegalArgumentException("invalid uri for device");
		}
		
		checkURI(deviceUri);
		if (deviceDao.findByDeviceUri(deviceUri) != null) {
			throw new DuplicateDeviceException(
					"storage device with the given uri already exists");
		}
		StorageDevice device = new StorageDevice();
		device.setDeviceUri(deviceUri);
		device.setStatus(StorageDevice.DeviceStatus.ON);
		return deviceDao.save(device);
	}

	/**
	 * 更新一个存储设备
	 * 
	 * @param deviceId
	 *            要更新的存储设备的id
	 * @param deviceUri
	 *            更新后存储设备的路径(optional)
	 * @param status
	 *            更新后存储设备的状态 (optional)
	 * @return 更新后的StorageDevice 对象
	 */
	public StorageDevice updateDevice(Long deviceId, String deviceUri,
			StorageDevice.DeviceStatus status) {
		StorageDevice device = deviceDao.findOne(deviceId);
		if (device == null) {
			throw new DeviceNotFoundException("device with id " + deviceId
					+ " not exists");
		}

		if (!Strings.isNullOrEmpty(deviceUri)) {
			checkURI(deviceUri);
			StorageDevice tempDevice = deviceDao.findByDeviceUri(deviceUri);
			if (tempDevice != null && tempDevice.getId() != device.getId()) {
				throw new DuplicateDeviceException(
						"storage device with the given uri already exists");
			}
			device.setDeviceUri(deviceUri);
		}

		if (status != null) {
			if (status.equals(StorageDevice.DeviceStatus.ACTIVE)) {
				deviceDao.setActiveDeviceOn();
			}
			device.setStatus(status);
		}
		device = deviceDao.save(device);
		return device;
	}

	/**
	 * StorageDevice 的查询接口
	 * 
	 * @param query
	 * @param pageable
	 * @return
	 */
	public Page<StorageDevice> search(String query, Pageable pageable) {
		try {
			List<SearchFilter> filters = SearchFilter.parseQuery(query);
			Specification<StorageDevice> spec = Specifications.fromFilters(
					filters, StorageDevice.class);
			return deviceDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}

	/**
	 * 根据id 查找设备
	 * 
	 * @param id
	 *            设备id
	 * @return 设备对象
	 */
	public StorageDevice findDeviceById(Long id) {
		return deviceDao.findOne(id);
	}

	/**
	 * 检查URI 是否合法，以及当前系统是否支持URI 的schema
	 * 
	 * @param uri
	 * @return
	 */
	private URI checkURI(String uri) {
		URI tempUri = null;
		try {
			tempUri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("format of uri not correct");
		}

		// check schema
		StorageDevice.DeviceType schema = StorageDevice.DeviceType.of(tempUri
				.getScheme());
		if (schema == null) {
			throw new IllegalArgumentException(
					"schema of uri not specified or not supported");
		}

		// check path
		String path = tempUri.getPath();
		if (!path.startsWith("/")
				|| (schema.equals(StorageDevice.DeviceType.FILE) && WINDOWS && !path
						.substring(2, 3).equals(":"))) {
			throw new IllegalArgumentException("not an absolute path");
		}

		return tempUri;
	}
}
