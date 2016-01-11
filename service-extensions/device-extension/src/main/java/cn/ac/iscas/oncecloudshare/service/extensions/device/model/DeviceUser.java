package cn.ac.iscas.oncecloudshare.service.extensions.device.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_device_user", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "device_id"})})
public class DeviceUser extends IdEntity {
	public static enum Status{
		CREATED, APPLYING, ENABLE, DISABLED
	}
	
	private User user;
	private Device device;
	private Status status = Status.CREATED;
	// 审核人
	private User reviewBy;
	// 审核时间
	private Date reviewTime;
	// 登录次数
	private long loginTimes = 0; 
	
	public DeviceUser(){
		super();		
	}
	
	public DeviceUser(Device device, User user){
		super();	
		this.device = device;
		this.user = user;
	}

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne
	@JoinColumn(name = "device_id", nullable = false)
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}	

	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@ManyToOne
	@JoinColumn(name = "review_by")
	public User getReviewBy() {
		return reviewBy;
	}

	public void setReviewBy(User reviewBy) {
		this.reviewBy = reviewBy;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}

	public long getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(long loginTimes) {
		this.loginTimes = loginTimes;
	}
}