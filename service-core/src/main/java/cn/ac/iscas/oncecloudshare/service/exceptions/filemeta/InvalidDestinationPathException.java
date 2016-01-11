package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class InvalidDestinationPathException extends BusinessException {

	public InvalidDestinationPathException(){
	}

	public InvalidDestinationPathException(String message){
		super(message);
	}

	public InvalidDestinationPathException(String message, Throwable cause){
		super(message, cause);
	}

	public InvalidDestinationPathException(Throwable cause){
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.INVALID_DESTINATION_PATH;
	}
}
