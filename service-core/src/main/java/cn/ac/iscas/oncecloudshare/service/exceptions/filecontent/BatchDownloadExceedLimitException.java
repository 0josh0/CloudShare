package cn.ac.iscas.oncecloudshare.service.exceptions.filecontent;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;

public class BatchDownloadExceedLimitException extends BusinessException {

	public BatchDownloadExceedLimitException(){
	}

	public BatchDownloadExceedLimitException(String message){
		super(message);
	}

	public BatchDownloadExceedLimitException(String message, Throwable cause){
		super(message, cause);
	}

	public BatchDownloadExceedLimitException(Throwable cause){
		super(cause);
	}

	@Override
	public ErrorCode getErrorCode(){
		return ErrorCode.BATCH_DOWNLOAD_EXCEED_LIMIT;
	}
}
