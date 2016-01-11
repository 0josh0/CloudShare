package cn.ac.iscas.oncecloudshare.service.exceptions.account;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class DuplicateEmailException extends BusinessException {

	public DuplicateEmailException(){
	}

	public DuplicateEmailException(String message){
		super(message);
	}

	public DuplicateEmailException(String message, Throwable cause){
		super(message, cause);
	}

	public DuplicateEmailException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.DUPLICATE_EMAIL;
	}
}
