package cn.ac.iscas.oncecloudshare.service.exceptions.filemeta;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

/**
 * "合并临时文件片段"接口参数中的（部分）id不合法
 * 
 * @author Chen Hao
 */
public class InvalidFramentIdException extends BusinessException {

	public InvalidFramentIdException(){
	}

	public InvalidFramentIdException(String message){
		super(message);
	}

	public InvalidFramentIdException(String message, Throwable cause){
		super(message, cause);
	}

	public InvalidFramentIdException(Throwable cause){
		super(cause);
	}
	
	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.INVALID_FRAMENT_ID;
	}
}
