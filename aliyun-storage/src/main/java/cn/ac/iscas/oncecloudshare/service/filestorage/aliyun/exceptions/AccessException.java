package cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

@SuppressWarnings("serial")
public class AccessException extends BusinessException {
	public AccessException() {
		super();
	}

	public AccessException(String message) {
		super(message);
	}

	public AccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessException(Throwable cause) {
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode() {
		return ErrorCode.ALIYUN_ACCESS_ERROR;
	}
}
