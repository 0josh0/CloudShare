package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;

import com.google.common.collect.Lists;

@Entity
@Table(name = "ocs_workspace")
public class Workspace extends DescriptionEntity {
	// 访问权限
	public AccessModifier accessModifier = AccessModifier.PUBLIC;
	// 名称
	private String name;
	// 小组
	private WorkspaceTeam team;
	// 空间
	private WorkspaceSpace space;
	// 申请时间
	private Date applyTime;
	// 申请人
	private User applyBy;
	// 状态
	private String status;
	// 工作空间主人
	private User owner;
	// 默认成员角色
	private String defaultMemberRole;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public AccessModifier getAccessModifier() {
		return accessModifier;
	}

	public void setAccessModifier(AccessModifier accessModifier) {
		this.accessModifier = accessModifier;
	}

	@Column(length = 32)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToOne
	@JoinColumn(name = "team_id")
	public WorkspaceTeam getTeam() {
		return team;
	}

	public void setTeam(WorkspaceTeam team) {
		this.team = team;
	}

	@OneToOne
	@JoinColumn(name = "space_id")
	public WorkspaceSpace getSpace() {
		return space;
	}

	public void setSpace(WorkspaceSpace space) {
		this.space = space;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}

	@ManyToOne
	@JoinColumn(name = "apply_by")
	public User getApplyBy() {
		return applyBy;
	}

	public void setApplyBy(User applyBy) {
		this.applyBy = applyBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@ManyToOne
	@JoinColumn(name = "owner_id")
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getDefaultMemberRole() {
		return defaultMemberRole;
	}

	public void setDefaultMemberRole(String defaultMemberRole) {
		this.defaultMemberRole = defaultMemberRole;
	}

	/**
	 * 判断用户是否是工作空间成员
	 * 
	 * @param userId
	 * @return
	 */
	@Transient
	public boolean hasMember(long userId) {
		if (getTeam() == null) {
			return false;
		}
		for (TeamMate mate : getTeam().getMembers()) {
			if (mate.getUser().getId() == userId) {
				return true;
			}
		}
		return false;
	}
	
	@Transient
	public TeamMate getMember(long userId) {
		if (getTeam() == null) {
			return null;
		}
		for (TeamMate mate : getTeam().getMembers()) {
			if (mate.getUser().getId() == userId) {
				return mate;
			}
		}
		return null;
	}

	public boolean hasFile(SpaceFile file) {
		return getSpace() != null && getSpace().hasFile(file);
	}
	
	@Transient
	public List<Long> getMemberUserIds(){
		List<Long> userIds = Lists.newArrayList();
		if (getTeam() != null && getTeam().getMembers() != null){
			for (TeamMate mate : getTeam().getMembers()){
				userIds.add(mate.getUser().getId());
			}
		}
		return userIds;
	}
}
