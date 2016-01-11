package cn.ac.iscas.oncecloudshare.service.model.share;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

/**
 * 分享的目标
 * 
 * @author cly
 * @version  
 * @since JDK 1.6
 */
@Entity
@Table(name = "ocs_share_recipient")
public class ShareRecipient extends IdEntity{	
	private Share share;
	// 目标的类型
	private String type;
	// 目标的标识
	private Long identify;
	// 显示名
	private String displayName;
	
	
	@ManyToOne
	@JoinColumn(name = "share_id", nullable = false)
	public Share getShare() {
		return share;
	}

	public void setShare(Share share) {
		this.share = share;
	}

	@Column(length = 16, nullable = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(nullable = false)
	public Long getIdentify() {
		return identify;
	}

	public void setIdentify(Long identify) {
		this.identify = identify;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
