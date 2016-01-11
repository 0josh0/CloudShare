package cn.ac.iscas.oncecloudshare.service.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

public class DefaultBusinessException extends BusinessException {
	private static final long serialVersionUID = -677964952107753739L;

	private ErrorCode errorCode;

	public DefaultBusinessException(ErrorCode errorCode) {
		super(Gsons.defaultGsonNoPrettify().toJson(errorCode));
		this.errorCode = errorCode;
	}

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}