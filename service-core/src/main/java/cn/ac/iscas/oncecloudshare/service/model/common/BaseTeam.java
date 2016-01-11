package cn.ac.iscas.oncecloudshare.service.model.common;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Lists;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;

@Entity
@Table(name = "ocs_team")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class BaseTeam extends DescriptionEntity {
	// 小组成员
	private List<TeamMate> members = Lists.newArrayList();
	// 成员个数
	private Integer membersCount;
	
	private String type;

	private Status status = Status.ACTIVE;
	
	private Long roomId;

	public Integer getMembersCount() {
		return membersCount;
	}

	public void setMembersCount(Integer membersCount) {
		this.membersCount = membersCount;
	}

	@OneToMany(mappedBy = "team")
	public List<TeamMate> getMembers() {
		return members;
	}

	public void setMembers(List<TeamMate> members) {
		this.members = members;
	}

	@Column(nullable = false, insertable = false, updatable = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Transient
	public boolean hasMember(long userId) {
		return getMember(userId) != null;
	}
	
	@Transient
	public TeamMate getMember(long userId){
		for (TeamMate teamMate : getMembers()) {
			if (teamMate.getUser().getId() == userId) {
				return teamMate;
			}
		}
		return null;
	}
	
	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public enum Status {
		ACTIVE, DEACTIVE, DELETED
	}
}