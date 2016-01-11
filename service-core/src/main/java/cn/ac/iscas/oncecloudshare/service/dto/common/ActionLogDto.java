package cn.ac.iscas.oncecloudshare.service.dto.common;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;

public class ActionLogDto {
	public Long id;
	public UserDto user;
	public String type;
	public String targetType;
	public String targetId;
	public String params;
	public String description;
	public Long createTime;

	public static Function<ActionLog, ActionLogDto> forUser = new Function<ActionLog, ActionLogDto>() {
		public ActionLogDto apply(ActionLog input) {
			if (input == null) {
				return null;
			}
			ActionLogDto output = new ActionLogDto();
			output.id = input.getId();
			output.type = input.getType();
			output.targetType = input.getTargetType();
			output.targetId = input.getTargetId();
			output.params = input.getParams();
			output.description = input.getDescription();
			if (input.getCreateTime() != null) {
				output.createTime = input.getCreateTime().getTime();
			}
			return output;
		}
	};

	public static Function<ActionLog, ActionLogDto> forAdmin = new Function<ActionLog, ActionLogDto>() {
		public ActionLogDto apply(ActionLog input) {
			if (input == null) {
				return null;
			}
			ActionLogDto output = forUser.apply(input);
			if (input.getUser() != null) {
				output.user = new UserDto();
				output.user.id = input.getUser().getId();
				output.user.name = input.getUser().getName();
			}
			return output;
		}
	};
}
