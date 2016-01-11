package cn.ac.iscas.oncecloudshare.service.model.account;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name="ocs_remember_me")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class RememberMe extends IdEntity {
	private String token;
	private Date expireAt;
	private User user;

	@Column(nullable = false, unique = true)
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public void setExpireAt(Date expireAt) {
		this.expireAt = expireAt;
	}

	public void setUser(User user) {
		this.user = user;
	}
	

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}

	public Date getExpireAt() {
		return expireAt;
	}
}
