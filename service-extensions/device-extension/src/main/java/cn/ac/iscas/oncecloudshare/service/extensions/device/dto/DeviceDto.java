package cn.ac.iscas.oncecloudshare.service.extensions.device.dto;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import cn.ac.iscas.oncecloudshare.service.extensions.device.model.Device;

import com.google.common.base.Function;

public class DeviceDto {
	public String mac;
	public String type;
	public String hardware;
	public String osType;
	public String osVersion;

	public static Function<HttpServletRequest, DeviceDto> RequestTransformer = new Function<HttpServletRequest, DeviceDto>() {
		public DeviceDto apply(HttpServletRequest request) {
			DeviceDto output = new DeviceDto();
			// mac
			output.mac = request.getHeader("X-Device-Mac");
			if (StringUtils.isEmpty(output.mac)) {
				output.mac = Device.Type.undefined.toString();
			} else {
				output.mac = output.mac.toLowerCase();
			}
			// type
			output.type = request.getHeader("X-Device-Type");
			if (StringUtils.isEmpty(output.type) || Device.Type.valueOf(output.type) == null) {
				output.type = Device.Type.undefined.toString();
				output.mac = output.type;
			}
			// hardware
			output.hardware = request.getHeader("X-Device-Hardware");
			// osType and osVersion
			output.osType = request.getHeader("X-Device-OS-Type");
			output.osVersion = request.getHeader("X-Device-OS-Version");

			return output;
		}
	};
	
	public static Function<Device, DeviceDto> DefaultTransformer = new Function<Device, DeviceDto>() {
		public DeviceDto apply(Device input) {
			if (input == null){
				return null;
			}
			DeviceDto output = new DeviceDto();
			output.mac = input.getMac();
			output.type = input.getType().toString();
			output.hardware = input.getHardware();
			output.osType = input.getOsType();
			output.osVersion = input.getOsVersion();
			return output;
		}
	};
}
