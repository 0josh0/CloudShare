package cn.ac.iscas.oncecloudshare.service.filestorage.advance.dto;

import java.util.Date;
import java.util.List;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice.DeviceStatus;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.util.FileUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class StorageDeviceDto {

	public static final Function<StorageDevice, StorageDeviceDto> TRANSFORMER = new Function<StorageDevice, StorageDeviceDto>() {

		@Override
		public StorageDeviceDto apply(StorageDevice input) {
			StorageDeviceDto dto = new StorageDeviceDto();
			dto.id = input.getId();
			dto.deviceUri = input.getDeviceUri();
			dto.status = input.getStatus();
			dto.totalSpace = FileUtils.getTotalSpace(input.getDeviceUri());
			dto.freeSpace = FileUtils.getFreeSpace(input.getDeviceUri());
			dto.createTime = input.getCreateTime();
			dto.updateTime = input.getUpdateTime();
			return dto;
		}
	};

	public Long id;
	public String deviceUri;
	public DeviceStatus status;
	public Long totalSpace;
	public Long freeSpace;
	public Date createTime;
	public Date updateTime;

	public static StorageDeviceDto of(StorageDevice device) {
		return TRANSFORMER.apply(device);
	}

	public static List<StorageDeviceDto> of(List<StorageDevice> devices) {
		return Lists.transform(devices, TRANSFORMER);
	}

}
