package cn.ac.iscas.oncecloudshare.messaging.exceptions;

import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.ErrorCode;

public class SearchException extends BusinessException {

	public SearchException(){
	}

	public SearchException(String message){
		super(message);
	}

	public SearchException(String message, Throwable cause){
		super(message, cause);
	}

	public SearchException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return BasicErrorCode.INVALID_SEARCH_QUERY;
	}
}
