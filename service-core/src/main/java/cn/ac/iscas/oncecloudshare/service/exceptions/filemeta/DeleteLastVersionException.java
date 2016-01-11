package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class DeleteLastVersionException extends BusinessException {

	public DeleteLastVersionException(){
	}

	public DeleteLastVersionException(String message){
		super(message);
	}

	public DeleteLastVersionException(String message, Throwable cause){
		super(message, cause);
	}

	public DeleteLastVersionException(Throwable cause){
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.DELETE_LAST_VERSION;
	}
}
