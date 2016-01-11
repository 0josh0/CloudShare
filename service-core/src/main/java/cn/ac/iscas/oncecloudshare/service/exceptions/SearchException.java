package cn.ac.iscas.oncecloudshare.service.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

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
		return ErrorCode.INVALID_SEARCH_QUERY;
	}
}
