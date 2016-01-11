package cn.ac.iscas.oncecloudshare.service.model.account;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam;

@Entity
@DiscriminatorValue("system")
public class Team extends BaseTeam {
	// 名称
	private String name;
	// 创建者
	private User createBy;

	@Column(length = 64)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "create_by")
	public User getCreateBy() {
		return createBy;
	}

	public void setCreateBy(User createBy) {
		this.createBy = createBy;
	}
}