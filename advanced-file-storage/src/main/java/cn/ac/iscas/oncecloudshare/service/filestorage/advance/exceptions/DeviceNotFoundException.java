package cn.ac.iscas.oncecloudshare.service.filestorage.advance.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

@SuppressWarnings("serial")
public class DeviceNotFoundException extends BusinessException {

	public DeviceNotFoundException(String message) {
		super(message);
	}

	public DeviceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeviceNotFoundException(Throwable cause) {
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode() {
		return ErrorCode.NOT_FOUND;
	}
}
