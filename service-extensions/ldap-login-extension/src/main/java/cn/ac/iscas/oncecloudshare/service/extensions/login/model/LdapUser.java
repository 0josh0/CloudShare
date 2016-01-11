package cn.ac.iscas.oncecloudshare.service.extensions.login.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_ldap_user")
public class LdapUser extends IdEntity{
	private User user;
	private String ldapPrincipal;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(nullable = false, unique = true)
	public String getLdapPrincipal() {
		return ldapPrincipal;
	}

	public void setLdapPrincipal(String ldapPrincipal) {
		this.ldapPrincipal = ldapPrincipal;
	}
}