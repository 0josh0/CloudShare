package cn.ac.iscas.oncecloudshare.service.model.contact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;

import org.hibernate.annotations.Index;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.contact.InviteCodeConstance.InviteCodeStatus;
import cn.ac.iscas.oncecloudshare.service.utils.CodeUtils;
import cn.ac.iscas.oncecloudshare.service.utils.DateUtils;

/**
 * 
 * 邀请码实体类
 * 
 * @author One
 * 
 */

@Entity
@Table(name = "ocs_invite_code", schema = "ocs_service", uniqueConstraints = { @UniqueConstraint(columnNames = { "inviter_id",
		"accepter_id" }) })
public class InviteCode extends IdEntity {

	/**
	 * 是否已经使用
	 */
	private InviteCodeStatus used;

	/**
	 * 过期时间
	 */
	private long expireAt = DateUtils.NEVER_EXPIRE_MILLIS;

	/**
	 * 邀请码实体
	 */
	private String inviteCode;

	/**
	 * 邀请人ID
	 */
	private User inviter;

	/**
	 * 被邀请人ID
	 */
	private User accepter;

	@ManyToOne
	@JoinColumn(name = "inviter_id", nullable = false, updatable = false)
	public User getInviter() {
		return inviter;
	}

	public void setInviter(User inviter) {
		this.inviter = inviter;
	}

	@ManyToOne
	@JoinColumn(name = "accepter_id")
	public User getAccepter() {
		return accepter;
	}

	public void setAccepter(User accepter) {
		this.accepter = accepter;
	}

	@Column(nullable = false)
	@Min(value = 0)
	public long getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(long expireAt) {
		this.expireAt = expireAt;
	}

	@Index(name = "inviteCode")
	@Column(nullable = false, length =40)
	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	@Column(name = "status", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	public InviteCodeStatus getUsed() {
		return used;
	}

	public void setUsed(InviteCodeStatus used) {
		this.used = used;
	}

	@Override
	@PrePersist
	protected void onCreate() {
		super.onCreate();
		this.inviteCode = CodeUtils.getCodeBody();
	}

}
