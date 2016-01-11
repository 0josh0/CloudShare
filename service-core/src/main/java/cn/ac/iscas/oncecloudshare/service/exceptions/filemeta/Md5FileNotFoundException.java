package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class Md5FileNotFoundException extends BusinessException {

	public Md5FileNotFoundException(){
	}

	public Md5FileNotFoundException(String message){
		super(message);
	}

	public Md5FileNotFoundException(String message, Throwable cause){
		super(message, cause);
	}

	public Md5FileNotFoundException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.MD5_FILE_NOT_FOUND;
	}
}
