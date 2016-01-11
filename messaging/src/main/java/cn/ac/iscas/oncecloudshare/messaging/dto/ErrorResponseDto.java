package cn.ac.iscas.oncecloudshare.messaging.dto;

import org.springframework.http.HttpStatus;


public class ErrorResponseDto extends ResponseDto{

	private Integer errorCode;
	
	public ErrorResponseDto(HttpStatus httpStatus, String message){
		super(httpStatus,message);
		this.errorCode=httpStatus.value()*100;
	}
	
	public ErrorResponseDto(ErrorCode errorCode){
		super(HttpStatus.valueOf(errorCode.getStatusCode()),errorCode.getMessage());
		this.errorCode=errorCode.getErrorCode();
	}
	
	public ErrorResponseDto(ErrorCode errorCode,String message){
		super(HttpStatus.valueOf(errorCode.getStatusCode()),message);
		this.errorCode=errorCode.getErrorCode();
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
}
