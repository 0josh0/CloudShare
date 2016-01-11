package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class InvalidPathException extends BusinessException {

	public InvalidPathException(){
	}

	public InvalidPathException(String message){
		super(message);
	}

	public InvalidPathException(String message, Throwable cause){
		super(message, cause);
	}

	public InvalidPathException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.INVALID_PATH;
	}
}
