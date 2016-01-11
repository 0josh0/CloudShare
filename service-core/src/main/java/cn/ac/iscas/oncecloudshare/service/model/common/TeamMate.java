package cn.ac.iscas.oncecloudshare.service.model.common;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;

@Entity
@Table(name = "ocs_team_mate", uniqueConstraints = {@UniqueConstraint(columnNames = {"team_id", "user_id"})})
public class TeamMate extends IdEntity {
	// 小组
	private BaseTeam team;
	// 用户
	private User user;
	// 用户显示名
	private String displayName;
	// 加入时间
	private Date joinTime;
	// 角色
	private String role;
	
	public TeamMate(){
	}
	
	public TeamMate(BaseTeam team, User user) {
		this(team, user, null, null);
	}
	
	public TeamMate(BaseTeam team, User user, String displayName) {
		this(team, user, displayName, null);
	}

	public TeamMate(BaseTeam team, User user, String displayName, String role) {
		super();
		this.team = team;
		this.user = user;
		this.displayName = displayName;
		this.joinTime = new Date();
		this.role = role;
	}

	@ManyToOne
	@JoinColumn(name = "team_id", updatable = false, nullable = false)
	public BaseTeam getTeam() {
		return team;
	}

	public void setTeam(BaseTeam team) {
		this.team = team;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", updatable = false, nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(length = 64)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	

	@Temporal(TemporalType.TIMESTAMP)
	public Date getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public static Function<TeamMate, Long> TO_USERID = new Function<TeamMate, Long>() {		
		@Override
		@Nullable
		public Long apply(@Nullable TeamMate input) {
			if (input.getUser() != null && UserStatus.ACTIVE.equals(input.getUser().getStatus())){
				return input.getUser().getId();
			}
			return null;
		}
	};
}
