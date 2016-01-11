package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

/**
 * 工作空间文件标签
 * SpaceFile和File两个类最好重构成一个
 * 
 * @author cly
 * @version  
 * @since JDK 1.6
 */
@Entity
@Table(name = "ocs_space_tag", uniqueConstraints = @UniqueConstraint(columnNames = { "title", "space_id" }))
public class SpaceTag extends IdEntity {
	private String title;
	private BaseSpace owner;
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
	@JoinColumn(name = "space_id", nullable = false)
	public BaseSpace getOwner() {
		return owner;
	}

	public void setOwner(BaseSpace owner) {
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
