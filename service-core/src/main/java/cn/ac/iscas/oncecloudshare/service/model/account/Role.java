package cn.ac.iscas.oncecloudshare.service.model.account;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

@Embeddable
public class Role {

	String domain;
	String name;

	public Role() {
	}

	public Role(String domain, String name) {
		super();
		this.domain = domain;
		this.name = name;
	}

	@NotEmpty
	@Length(max=32)
	@Column(name="role_domain",nullable=false,length=32)
	public String getDomain(){
		return domain;
	}
	
	public void setDomain(String domain){
		this.domain=domain;
	}
	
	@NotEmpty
	@Length(max=64)
	@Column(name="role_name",nullable=false,length=64)
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		Role other = (Role) obj;
		return domain.equals(other.domain) && name.equals(other.name);
	}
}
