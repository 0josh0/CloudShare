package cn.ac.iscas.oncecloudshare.service.application.dto;

import cn.ac.iscas.oncecloudshare.service.application.model.AdminApplication;

import com.google.common.base.Function;

public class AdminApplicationDto extends ApplicationDto{
	public static final Function<AdminApplication, AdminApplicationDto> DEFAULT_TRANSFORMER = new Function<AdminApplication, AdminApplicationDto>() {
		@Override
		public AdminApplicationDto apply(AdminApplication input) {
			return ApplicationDto.defaultInit(input, new AdminApplicationDto());
		}
	};
}