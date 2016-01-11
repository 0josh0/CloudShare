package cn.ac.iscas.oncecloudshare.service.dto.account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

import cn.ac.iscas.oncecloudshare.service.model.account.Team;

import com.google.common.base.Function;

public class TeamDto {
	public long id;
	public String name;
	public UserDto createBy;
	public String description;
	public Long createTime;
	public Long roomId;
	
	public static class CreateRequest {
		@NotNull
		@Length(min = 1, max = 32)
		private String name;
		@Length(max = 255)
		private String description;
		
		private long[] members;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public long[] getMembers() {
			return members;
		}

		public void setMembers(long[] members) {
			this.members = members;
		}
	}

	public static Function<Team, TeamDto> toBrief = new Function<Team, TeamDto>() {
		public TeamDto apply(Team team) {
			TeamDto output = new TeamDto();
			output.id = team.getId();
			output.name = team.getName();
			if (team.getCreateBy()!= null) {
				output.createBy = UserDto.GLANCE_TRANSFORMER.apply(team.getCreateBy());
			}
			if (team.getCreateTime() != null) {
				output.createTime = team.getCreateTime().getTime();
			}
			output.description = team.getDescription();
			output.roomId = team.getRoomId();
			return output;
		}
	};

	// 邀请成员
	public static class InviteRequest {
		@NotNull
		@Size(min = 1)
		private long[] targets;
		private String message;

		public long[] getTargets() {
			return targets;
		}

		public void setTargets(long[] targets) {
			this.targets = targets;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	
	// 邀请成员的应答
	public static class InviteResponse {
		public boolean success;
		public String failureMessage;
		public long userId;
		public String displayName;
		public String role;
	}
}
