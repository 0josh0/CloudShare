package cn.ac.iscas.oncecloudshare.service.filestorage.advance.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

@SuppressWarnings("serial")
public class DuplicateDeviceException extends BusinessException {

	public DuplicateDeviceException(String message) {
		super(message);
	}

	public DuplicateDeviceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateDeviceException(Throwable cause) {
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode() {
		return ErrorCode.CONFLICT;
	}
}
