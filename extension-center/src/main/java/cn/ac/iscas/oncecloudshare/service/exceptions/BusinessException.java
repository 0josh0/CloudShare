package cn.ac.iscas.oncecloudshare.service.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;

/**
 * 业务逻辑异常
 * @author Chen Hao
 */
public abstract class BusinessException extends RuntimeException {

	public BusinessException(){
	}

	public BusinessException(String message){
		super(message);
	}

	public BusinessException(String message, Throwable cause){
		super(message, cause);
	}

	public BusinessException(Throwable cause){
		super(cause);
	}
	
	public abstract ErrorCode getErrorCode();
}
