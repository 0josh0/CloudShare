package cn.ac.iscas.oncecloudshare.service.extensions.device.dto;

import cn.ac.iscas.oncecloudshare.service.model.account.User;

import com.google.common.base.Function;

public class UserDto {
	public Long id;
	public String name;
	public Long departmentId;
	public String departmentName;

	public static final Function<User, UserDto> DefaultTranformer = new Function<User, UserDto>() {
		public UserDto apply(User input) {
			if (input == null) {
				return null;
			}
			UserDto output = new UserDto();
			output.id = input.getId();
			output.name = input.getName();
			if (input.getDepartment() != null) {
				output.departmentId = input.getDepartment().getId();
				output.departmentName = input.getDepartment().getName();
			}
			return output;
		}
	};
}
