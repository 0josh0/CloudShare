package cn.ac.iscas.oncecloudshare.service.model.account;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Strings;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name="ocs_role",uniqueConstraints=
	@UniqueConstraint(columnNames={"user_id","role_domain","role_name","target"})
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class RoleEntry extends IdEntity{

	User user;
	Role role;
	String target;
	
	@NotNull
	@ManyToOne(optional=false)
	@JoinColumn(name="user_id")
	public User getUser(){
		return user;
	}
	
	public void setUser(User user){
		this.user=user;
	}
	
	@Embedded
	public Role getRole(){
		return role;
	}
	
	public void setRole(Role role){
		this.role=role;
	}
	
	@Column(length=32)
	public String getTarget(){
		return target;
	}
	
	public void setTarget(String target){
		this.target=target;
	}
	
	public String toShiroRoleIdentifier(){
		StringBuilder sb=new StringBuilder(role.getDomain());
		sb.append(":"+role.getName());
		if(!Strings.isNullOrEmpty(target)){
			sb.append(":"+target);
		}
		return sb.toString();
	}
}
