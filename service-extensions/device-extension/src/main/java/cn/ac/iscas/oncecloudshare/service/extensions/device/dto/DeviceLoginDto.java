package cn.ac.iscas.oncecloudshare.service.extensions.device.dto;

import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceLogin;

import com.google.common.base.Function;

public class DeviceLoginDto {
	public UserDto user;
	public DeviceDto device;
	public Long time;
	public String ip;
	public String location;

	public static final Function<DeviceLogin, DeviceLoginDto> AdminTransformer = new Function<DeviceLogin, DeviceLoginDto>() {
		public DeviceLoginDto apply(DeviceLogin input) {
			if (input == null) {
				return null;
			}
			DeviceLoginDto output = new DeviceLoginDto();
			if (input.getDeviceUser() != null) {
				output.user = UserDto.DefaultTranformer.apply(input.getDeviceUser().getUser());
				output.device = DeviceDto.DefaultTransformer.apply(input.getDeviceUser().getDevice());
			}
			if (input.getCreateTime() != null) {
				output.time = input.getCreateTime().getTime();
			}
			output.ip = input.getIp();
			output.location = input.getLocation();
			return output;
		}
	};
}
