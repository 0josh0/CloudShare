package cn.ac.iscas.oncecloudshare.service.exceptions.account;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class DuplicateDepartmentRouteException extends BusinessException {

	public DuplicateDepartmentRouteException(){
	}

	public DuplicateDepartmentRouteException(String message){
		super(message);
	}

	public DuplicateDepartmentRouteException(String message, Throwable cause){
		super(message, cause);
	}

	public DuplicateDepartmentRouteException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.DUPLICATE_EMAIL;
	}
}
