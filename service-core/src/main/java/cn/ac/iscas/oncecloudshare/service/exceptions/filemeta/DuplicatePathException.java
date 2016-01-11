package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class DuplicatePathException extends BusinessException {

	public DuplicatePathException(){
	}

	public DuplicatePathException(String message){
		super(message);
	}

	public DuplicatePathException(String message, Throwable cause){
		super(message, cause);
	}

	public DuplicatePathException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.DUPLICATE_PATH;
	}
}
