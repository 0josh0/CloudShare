package cn.ac.iscas.oncecloudshare.service.model.account;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

@Entity
@Table(name="ocs_depatment")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Department extends IdEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8419442334884037103L;

	private String name;

	@GsonHidden
	private Department parent;

	private String route;
	
	private List<Department> children;

	private List<User> users;

	public Department(){
	}

	public Department(String name){
		this.name=name;
	}

	@NotNull
	@Length(min=1,max=256)
	// @Column(nullable=false,length=256)
	public String getRoute(){
		return route;
	}

	public void setRoute(String route){
		route=FilePathUtil.normalizePath(route);
		if(route!=null){
			this.name=FilePathUtil.extractFilenameFromPath(route);
		}
		this.route=route;
	}

	@NotNull
	@Length(min=1,max=32)
	// @Column(nullable=false,length=32)
	public String getName(){
		return name;
	}

	protected void setName(String name){
		this.name=name;
	}

	@ManyToOne()
	public Department getParent(){
		return parent;
	}

	public void setParent(Department parent){
		this.parent=parent;
	}

	@OneToMany(mappedBy="parent")
	public List<Department> getChildren(){
		return children;
	}

	public void setChildren(List<Department> children){
		this.children=children;
	}

	@OneToMany(mappedBy="department")
	public List<User> getUsers(){
		return users;
	}

	public void setUsers(List<User> users){
		this.users=users;
	}

}
