package cn.ac.iscas.oncecloudshare.service.extensions.device.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;
import cn.ac.iscas.oncecloudshare.service.extensions.device.utils.DeviceUtils;

public class DeviceDisabledException extends BusinessException{
	private static final long serialVersionUID = 2394631515755756527L;
	
	@Override
	public ErrorCode getErrorCode() {
		return DeviceUtils.ErrorCodes.DEVICE_DISABLED;
	}
}
