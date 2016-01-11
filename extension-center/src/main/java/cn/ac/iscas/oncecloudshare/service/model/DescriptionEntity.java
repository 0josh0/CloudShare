package cn.ac.iscas.oncecloudshare.service.model;

import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.Length;

@MappedSuperclass
public class DescriptionEntity extends IdEntity {

	protected String description;

	@Length(max=256)
	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description=description;
	}

}
