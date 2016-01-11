package cn.ac.iscas.oncecloudshare.service.model.filemeta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_tag", uniqueConstraints = @UniqueConstraint(columnNames = { "title", "owner_id" }))
public class Tag extends IdEntity {
	private String title;
	private User owner;
	private Integer orderIndex = 0;
	private Long filesCount = 0L;

	@Column(nullable = false, length = 32)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Column(nullable = false)
	public Integer getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex;
	}

	@Column(nullable = false)
	public Long getFilesCount() {
		return filesCount;
	}

	public void setFilesCount(Long filesCount) {
		this.filesCount = filesCount;
	}
}
