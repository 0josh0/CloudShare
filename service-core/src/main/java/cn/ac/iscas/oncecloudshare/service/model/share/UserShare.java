package cn.ac.iscas.oncecloudshare.service.model.share;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_share_user", uniqueConstraints = {@UniqueConstraint(columnNames = {"file_id", "owner_id", "recipient_id"})})
public class UserShare extends AbstractShare {
	// 文件分享的目标
	private User recipient;

	public UserShare() {
	}

	@ManyToOne
	@JoinColumn(name = "recipient_id", nullable = false, updatable = false)
	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}
}
