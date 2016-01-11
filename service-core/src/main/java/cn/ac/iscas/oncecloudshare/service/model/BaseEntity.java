package cn.ac.iscas.oncecloudshare.service.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

@MappedSuperclass
public class BaseEntity   {

	@GsonHidden
	protected Date createTime;

	@GsonHidden
	protected Date updateTime;

	private Date currentDateNoMillis(){
		long time=System.currentTimeMillis()/1000*1000;
		return new Date(time);
	}
	
	@PrePersist
	protected void onCreate(){
		createTime=updateTime=currentDateNoMillis();
	}
	
	@PreUpdate
	protected void onUpdate(){
		updateTime=currentDateNoMillis();
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreateTime(){
		return createTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime=createTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getUpdateTime(){
		return updateTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime=updateTime;
	}

}
