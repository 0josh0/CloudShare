package cn.ac.iscas.oncecloudshare.service.messaging.exceptions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class MessageException extends BusinessException{
	private static final long serialVersionUID = 2824121691368418347L;

	public MessageException(Exception e){
		super("msg_server_conmmunicate_error", e);
	}

	@Override
	public ErrorCode getErrorCode() {
		return ErrorCode.INTERNAL_SERVER_ERROR;
	}
}