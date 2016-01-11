package cn.ac.iscas.oncecloudshare.messaging.model.iospush;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.messaging.model.IdEntity;

@Entity
@Table(name="ocs_ios_device")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class IOSDevice extends IdEntity {

	private Long userId;
	private String deviceToken;
	private String description;

	@Column (nullable=false,columnDefinition="BIGINT(11)")
	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId=userId;
	}

	@Column(nullable=false,columnDefinition="CHAR(64)")
	public String getDeviceToken(){
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken){
		this.deviceToken=deviceToken;
	}

	@Column(nullable=false,length=64)
	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description=description;
	}

}
