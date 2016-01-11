package cn.ac.iscas.oncecloudshare.service.filestorage.advance.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "ocs_storage_device")
public class StorageDevice extends IdEntity {
	private String deviceUri;
	private DeviceStatus status;

	public enum DeviceStatus {
		ACTIVE, ON, OFF;
		public static DeviceStatus of(String status) {
			for (DeviceStatus value : values()) {
				if (value.name().equalsIgnoreCase(status)) {
					return value;
				}
			}
			return null;
		}
	}

	public enum DeviceType {
		FILE;
		public static DeviceType of(String type) {
			for (DeviceType value : values()) {
				if (value.name().equalsIgnoreCase(type)) {
					return value;
				}
			}
			return null;
		}
	}

	@Column(nullable = false, unique = true)
	public String getDeviceUri() {
		return deviceUri;
	}

	public void setDeviceUri(String deviceUri) {
		this.deviceUri = deviceUri;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}
}
