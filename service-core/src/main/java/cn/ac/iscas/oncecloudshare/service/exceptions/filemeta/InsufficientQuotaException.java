package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class InsufficientQuotaException extends BusinessException {

	public InsufficientQuotaException(){
	}

	public InsufficientQuotaException(String message){
		super(message);
	}

	public InsufficientQuotaException(String message, Throwable cause){
		super(message, cause);
	}

	public InsufficientQuotaException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.INSUFFICIENT_QUOTA;
	}
}
