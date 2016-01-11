package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class FileUnmodifiableException extends BusinessException {

	public FileUnmodifiableException(){
	}

	public FileUnmodifiableException(String message){
		super(message);
	}

	public FileUnmodifiableException(String message, Throwable cause){
		super(message, cause);
	}

	public FileUnmodifiableException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.FILE_NOT_MODIFIABLE;
	}
}
