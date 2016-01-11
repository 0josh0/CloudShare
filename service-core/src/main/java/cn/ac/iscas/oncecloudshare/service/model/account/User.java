package cn.ac.iscas.oncecloudshare.service.model.account;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springside.modules.utils.Collections3;

import com.google.common.collect.Sets;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileOwner;

@Entity
@Table(name = "ocs_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends IdEntity implements FileOwner ,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7636869412840460583L;
	private String name;// 显示的用户名
	private String email;// 用户的email

	private String password;// 加密后的密码
	private String plainPassword;// 明文密码
	private String salt;// 加密的盐

	private Long quota;// 配额
	private Long restQuota;
	private Department department;
	private UserStatus status;
	private UserProfile profile;

	private Set<RoleEntry> roleEntries;

	/**
	 * 个性签名
	 */
	
	private String signature;

	@NotNull
	@Length(min = 1, max = 32)
	// @Column(nullable=false,length=32)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotEmpty
	@Email
	@Column(nullable = false, unique = true, length = 64)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(nullable = false, columnDefinition = "CHAR(40)")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// @NotNull
	@Length(min = 6, max = 32)
	@Transient
	public String getPlainPassword() {
		return plainPassword;
	}

	public void setPlainPassword(String plainPassword) {
		this.plainPassword = plainPassword;
	}

	@Column(nullable = false, columnDefinition = "CHAR(16)")
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@NotNull
	@Range(min = 0)
	// @Column(nullable=false)
	public Long getQuota() {
		return quota;
	}

	public void setQuota(Long quota) {
		this.quota = quota;
	}

	@NotNull
	@Range(min = 0)
	// @Column(nullable=false)
	public Long getRestQuota() {
		return restQuota;
	}

	public void setRestQuota(Long restQuota) {
		this.restQuota = restQuota;
	}

	@ManyToOne(optional = true)
	@JoinColumn(nullable = true, name = "department_id")
	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@NotNull
	@Column(nullable = false, length = 16)
	@Enumerated(EnumType.STRING)
	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@OneToOne(optional = true, mappedBy = "user")
	@OnDelete(action = OnDeleteAction.CASCADE)
	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}

	@OneToMany(orphanRemoval = true, mappedBy = "user", cascade = CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Set<RoleEntry> getRoleEntries() {
		return roleEntries;
	}

	public void setRoleEntries(Set<RoleEntry> roleEntries) {
		this.roleEntries = roleEntries;
	}

	@Column(nullable = true, length = 510)
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Transient
	public boolean hasRole(String domain, String role) {
		if (Collections3.isNotEmpty(roleEntries)) {
			Role compare = new Role(domain, role);
			for (RoleEntry entry : roleEntries) {
				if (entry.getRole().equals(compare)) {
					return true;
				}
			}
		}
		return false;
	}

	@Transient
	public boolean hasRole(Role role) {
		if (Collections3.isNotEmpty(roleEntries)) {
			for (RoleEntry entry : roleEntries) {
				if (entry.getRole().equals(role)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean addRole(Role role) {
		if (!hasRole(role)) {
			if (roleEntries == null) {
				roleEntries = Sets.newHashSet();
			}
			RoleEntry entry = new RoleEntry();
			entry.setRole(role);
			entry.setUser(this);
			roleEntries.add(entry);
			return true;
		}
		return false;
	}

	public boolean deleteRole(Role role) {
		if (Collections3.isNotEmpty(roleEntries)) {
			for (RoleEntry entry : roleEntries) {
				if (entry.getRole().equals(role)) {
					roleEntries.remove(entry);
					return true;
				}
			}
		}
		return false;
	}

}
