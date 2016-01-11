package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.AccessModifier;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;

import com.google.common.base.Function;

public class WorkspaceDto {
	@SuppressWarnings("unused")
	private static Logger _logger = LoggerFactory.getLogger(WorkspaceDto.class);

	public long id;
	public String name;
	public UserDto owner;
	public String description;
	public Long createTime;
	// 配额
	public Long quota;
	// 剩余的配额
	public Long restQuota;
	// 角色和权限
	public String role;
	public List<String> permissions;
	// 空间访问权限
	public String accessModifier;
	public String defaultMemberRole;
	public Long roomId;

	public static Function<Workspace, WorkspaceDto> glanceTransformer = new Function<Workspace, WorkspaceDto>() {
		public WorkspaceDto apply(Workspace input) {
			if (input == null) {
				return null;
			}
			WorkspaceDto brief = new WorkspaceDto();
			brief.id = input.getId();
			brief.name = input.getName();
			return brief;
		}
	};

	public static Function<Workspace, WorkspaceDto> WORKSPACE_TO_BRIEF = new Function<Workspace, WorkspaceDto>() {
		public WorkspaceDto apply(Workspace input) {
			WorkspaceDto brief = new WorkspaceDto();
			brief.id = input.getId();
			brief.name = input.getName();
			if (input.getOwner() != null) {
				brief.owner = UserDto.GLANCE_TRANSFORMER.apply(input.getOwner());
			}
			brief.description = input.getDescription();
			// 配额
			if (input.getSpace() != null) {
				brief.quota = input.getSpace().getQuota();
				brief.restQuota = input.getSpace().getRestQuota();
			}
			if (input.getApplyTime() != null) {
				brief.createTime = input.getApplyTime().getTime();
			}
			if (input.getAccessModifier() != null){
				brief.accessModifier = input.getAccessModifier().name();
			}
			brief.defaultMemberRole = input.getDefaultMemberRole();
			brief.roomId = input.getTeam().getRoomId();
			return brief;
		}
	};

	public static final Function<Workspace, WorkspaceDto> WORKSPACE_TO_DETAIL = new Function<Workspace, WorkspaceDto>() {
		public WorkspaceDto apply(Workspace input) {
			WorkspaceDto result = WORKSPACE_TO_BRIEF.apply(input);
			return result;
		}
	};

	// 邀请成员
	public static class InviteRequest {
		@NotNull
		@Size(min = 1)
		private long[] targets;
		private String message;
		// 角色
		private String role;

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

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}
	}

	public static class InviteResponse {
		public boolean success;
		public String failureMessage;
		public long userId;
		public String displayName;
		public String role;
	}

	public static class UpdateRequest {
		@NotNull
		@Length(min = 1, max = 32)
		private String name;
		@NotNull
		private AccessModifier accessModifier = AccessModifier.PUBLIC;
		@Pattern(regexp = "reader|limited_writer|writer|admin")
		private String defaultMemberRole;
		@Length(max = 255)
		private String description;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}		

		public AccessModifier getAccessModifier() {
			return accessModifier;
		}

		public void setAccessModifier(AccessModifier accessModifier) {
			this.accessModifier = accessModifier;
		}

		public String getDefaultMemberRole() {
			return defaultMemberRole;
		}

		public void setDefaultMemberRole(String defaultMemberRole) {
			this.defaultMemberRole = defaultMemberRole;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class UpdateMemberRequest {
		@Length(max = 64)
		private String displayName;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
	}

	public static class Member {
		@Deprecated
		public long userId;
		public String displayName;
		@Deprecated
		public String userEmail;
		public String role;
		public UserDto user;
		public List<String> permissions;

		public Member(TeamMate teamMate) {
			this.userId = teamMate.getUser().getId();
			this.userEmail = teamMate.getUser().getEmail();
			this.displayName = StringUtils.isEmpty(teamMate.getDisplayName()) ? teamMate.getUser().getName() : teamMate.getDisplayName();
			this.role = teamMate.getRole();
			this.user = UserDto.forAnon(teamMate.getUser());
		}
	}
}
