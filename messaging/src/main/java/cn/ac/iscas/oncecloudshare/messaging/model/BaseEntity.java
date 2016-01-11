package cn.ac.iscas.oncecloudshare.messaging.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public class BaseEntity {

	protected Date createTime;

	protected Date updateTime;
	
	@PrePersist
	protected void onCreate(){
		createTime=updateTime=new Date();
	}

	@PreUpdate
	protected void onUpdate(){
		updateTime=new Date();
	}

	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreateTime(){
		return createTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime=createTime;
	}

	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getUpdateTime(){
		return updateTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime=updateTime;
	}

}