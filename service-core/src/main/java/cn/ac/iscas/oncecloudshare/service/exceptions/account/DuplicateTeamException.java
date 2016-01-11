package cn.ac.iscas.oncecloudshare.service.exceptions.account;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class DuplicateTeamException extends BusinessException {
	private static final long serialVersionUID = 8445478105632340336L;

	public DuplicateTeamException() {
	}

	public DuplicateTeamException(String message) {
		super(message);
	}

	public DuplicateTeamException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateTeamException(Throwable cause) {
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode() {
		return ErrorCode.CONFLICT;
	}
}
