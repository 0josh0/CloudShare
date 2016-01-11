package cn.ac.iscas.oncecloudshare.service.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

@MappedSuperclass
public class BaseEntity {

	@GsonHidden
	protected Date createTime;

	@GsonHidden
	protected Date updateTime;

	@PrePersist
	protected void onCreate(){
		createTime=updateTime=new Date();
	}
	
	@Column(insertable=false,updatable=false,
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreateTime(){
		return createTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime=createTime;
	}

	@Column(insertable=false,updatable=false,
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getUpdateTime(){
		return updateTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime=updateTime;
	}

}
