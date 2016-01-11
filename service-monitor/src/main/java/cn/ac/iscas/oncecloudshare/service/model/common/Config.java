package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;
import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

/**
 * 系统配置
 * 
 * @author Chen Hao
 */
@Entity
@Table(name="ocs_config")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Config extends DescriptionEntity {

	public static AccessMode ADMIN_ACCESS_MODE_DEFAULT_VALUE=AccessMode.NONE;
	public static boolean NORMAL_USER_READABLE_DEFAULT_VALUE=false;
	public static DataType DATA_TYPE_DEFAULT_VALUE=DataType.STRING;
	
	public static enum AccessMode {
		READ_WRITE,
		READ_ONLY,
//		WRITE_ONLY,
		NONE
	}
	
	public static enum DataType{
		BOOLEAN,
		INT,
		LONG,
		DOUBLE,
		STRING,
	}

	String key;

	String value;
	
	DataType dataType=DATA_TYPE_DEFAULT_VALUE;

	/**
	 * 后台管理可否修改该配置项
	 */
	AccessMode adminAccessMode=ADMIN_ACCESS_MODE_DEFAULT_VALUE;

	/**
	 * 普通用户是否可读
	 */
	Boolean normalUserReadable=NORMAL_USER_READABLE_DEFAULT_VALUE;
	
	String displayGroup;
	
	@Column(name="config_key",nullable=false,unique=true,length=50)
	public String getKey(){
		return key;
	}

	public void setKey(String key){
		this.key=key;
	}

	@Column(nullable=false,length=256)
	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value=value;
	}

	@Column(nullable=false,length=16)
	@Enumerated(EnumType.STRING)
	public AccessMode getAdminAccessMode(){
		return adminAccessMode;
	}

	public void setAdminAccessMode(AccessMode adminAccessMode){
		this.adminAccessMode=adminAccessMode;
	}

	public boolean adminReadable(){
		return adminAccessMode==AccessMode.READ_WRITE ||
				adminAccessMode==AccessMode.READ_ONLY;
	}
	
	public boolean adminWritable(){
		return adminAccessMode==AccessMode.READ_WRITE;
	}

	@Column(columnDefinition="TINYINT(1) DEFAULT 0")
	public Boolean getNormalUserReadable(){
		return Objects.firstNonNull(normalUserReadable,NORMAL_USER_READABLE_DEFAULT_VALUE);
	}

	public void setNormalUserReadable(Boolean normalUserReadable){
		this.normalUserReadable=normalUserReadable;
	}

	@Column(nullable=false,length=16)
	@Enumerated(EnumType.STRING)
	public DataType getDataType(){
		return dataType;
	}
	
	public void setDataType(DataType dataType){
		this.dataType=dataType;
	}

	@Column(length=32)
	public String getDisplayGroup(){
		return displayGroup;
	}

	public void setDisplayGroup(String displayGroup){
		this.displayGroup=displayGroup;
	}
	
}
