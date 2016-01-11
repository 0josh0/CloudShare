package cn.ac.iscas.oncecloudshare.service.extensions.device.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "ocs_device_login")
public class DeviceLogin extends IdEntity {
	private DeviceUser deviceUser;
	private Date loginTime;
	private String ip;
	private String location;

	public DeviceLogin() {

	}

	@ManyToOne
	@JoinColumn(name = "device_user_id", nullable = false)
	public DeviceUser getDeviceUser() {
		return deviceUser;
	}

	public void setDeviceUser(DeviceUser deviceUser) {
		this.deviceUser = deviceUser;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}
}
