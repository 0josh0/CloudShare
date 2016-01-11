package cn.ac.iscas.oncecloudshare.service.extensions.sample;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

/**
 * 扩展中可以自定义entity,hibernate会自动建表
 * 
 * @author Chen Hao
 */
@Entity
@Table(name="ocs_sample")
public class SampleEntity extends IdEntity{

	String field;

	@Column(length=10)
	public String getField(){
		return field;
	}

	
	public void setField(String field){
		this.field=field;
	}
	
	
}
