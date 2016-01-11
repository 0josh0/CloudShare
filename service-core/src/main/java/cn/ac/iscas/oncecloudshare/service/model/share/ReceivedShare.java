package cn.ac.iscas.oncecloudshare.service.model.share;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_receivedshare")
public class ReceivedShare extends IdEntity {
	private Share share;
	private User recipient;
	// 为True时表示接受者删除了该分享
	private Boolean isDeleted = Boolean.FALSE;
	// 该分享用户属于分享的接受者中的哪一个
	private List<ShareRecipient> belongsTo;

	@ManyToOne
	@JoinColumn(name = "share_id", nullable = false)
	public Share getShare() {
		return share;
	}

	public void setShare(Share share) {
		this.share = share;
	}

	@ManyToOne
	@JoinColumn(name = "recipient_id", nullable = false)
	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	@Column(nullable = false)
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "ocs_receivedshare_belongsto", joinColumns = { @JoinColumn(name = "receivedshare_id") }, inverseJoinColumns = { @JoinColumn(name = "recipient_id") })
	public List<ShareRecipient> getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(List<ShareRecipient> belongsTo) {
		this.belongsTo = belongsTo;
	}
}