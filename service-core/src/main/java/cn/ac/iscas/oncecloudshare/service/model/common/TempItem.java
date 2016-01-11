package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name="ocs_temp_item")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class TempItem extends IdEntity {

	private String key;
	private String type;
	private String content;
	private Long expiresAt;

	@NotEmpty
	@Length(max=32)
	@Column(name="k",nullable=false,unique=true)
	public String getKey(){
		return key;
	}

	public void setKey(String key){
		this.key=key;
	}

	@NotEmpty
	@Length(max=16)
	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type=type;
	}

	@Length(max=1024)
	public String getContent(){
		return content;
	}

	public void setContent(String content){
		this.content=content;
	}

	@NotNull
	public Long getExpiresAt(){
		return expiresAt;
	}

	public void setExpiresAt(Long expiresAt){
		this.expiresAt=expiresAt;
	}

}
