package cn.ac.iscas.oncecloudshare.messaging.exceptions;

import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.ErrorCode;

public class RestServerException extends BusinessException {

	public RestServerException(){
	}

	public RestServerException(String message){
		super(message);
	}

	public RestServerException(String message, Throwable cause){
		super(message, cause);
	}

	public RestServerException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return BasicErrorCode.INVALID_SEARCH_QUERY;
	}
}
