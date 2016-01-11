package cn.ac.iscas.oncecloudshare.service.model.multitenancy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;

@Entity
@Table(name="ocs_global_user",schema=Constants.GLOBAL_SCHEMA)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class GlobalUser extends IdEntity {

	private String email;
	private Long tenantId;

	@NotEmpty
	@Email
	@Column(nullable=false,unique=true,length=128)
	public String getEmail(){
		return email;
	}

	public void setEmail(String email){
		this.email=email;
	}
	
	@NotNull
	public Long getTenantId(){
		return tenantId;
	}
	
	public void setTenantId(Long tenantId){
		this.tenantId=tenantId;
	}

}
