package cn.ac.iscas.oncecloudshare.service.filestorage.advance.security.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

@SuppressWarnings("serial")
public class EncryptionException extends BusinessException {

	public EncryptionException(String message) {
		super(message);
	}

	public EncryptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(Throwable cause) {
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode() {
		return ErrorCode.NOT_FOUND;
	}

}
