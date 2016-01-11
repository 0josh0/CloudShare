package cn.ac.iscas.oncecloudshare.service.dto.account;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;

import com.google.common.base.Function;

public class TeamMateDto {
	public UserDto user;
	public String role;
	// 用户显示名
	public String displayName;
	// 加入时间
	public Long joinTime;
	public List<String> permissions;

	public static Function<TeamMate, TeamMateDto> DEFAULT_TRANSFORMER = new Function<TeamMate, TeamMateDto>() {
		@Override
		public TeamMateDto apply(TeamMate input) {
			TeamMateDto output = new TeamMateDto();
			if (input.getUser() != null) {
				output.user = UserDto.GLANCE_TRANSFORMER.apply(input.getUser());
			}
			output.role = input.getRole();
			output.displayName = input.getDisplayName();
			if (input.getJoinTime() != null) {
				output.joinTime = input.getJoinTime().getTime();

			}
			return output;
		}
	};
}