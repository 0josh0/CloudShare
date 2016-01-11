package cn.ac.iscas.oncecloudshare.service.exceptions.rest;

import org.springframework.http.HttpStatus;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorResponseDto;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;


public class RestException extends RuntimeException{

	private static final long serialVersionUID=1L;
	
	public ErrorResponseDto errorResponseDto;
	
	public RestException(int statusCode,String message) {
		HttpStatus status=HttpStatus.valueOf(statusCode);
		Preconditions.checkArgument(status!=null,"invalid status code: "+statusCode);
		this.errorResponseDto=new ErrorResponseDto(status,message);
	}
	
	public RestException(HttpStatus status,String message) {
		this(new ErrorResponseDto(status,message));
	}
	
	public RestException(ErrorCode errorCode) {
		this(errorCode,null);
	}
	
	public RestException(ErrorCode errorCode,String message) {
		HttpStatus status=HttpStatus.valueOf(errorCode.statusCode);
		message=Objects.firstNonNull(message,errorCode.message);
		this.errorResponseDto=new ErrorResponseDto(errorCode,message);
	}
	
	public RestException(ErrorResponseDto errorResponseDto){
		super(errorResponseDto.getMessage());
		this.errorResponseDto=errorResponseDto;
	}
	
	public HttpStatus getHttpStatus(){
		return errorResponseDto.getHttpStatus();
	}
//	
//	public ErrorResponseDto getErrorResponseDto(){
//		return errorResponseDto;
//	}
//	
//	public String getResponseBody(){
//		return Gsons.defaultGson().toJson(errorResponseDto);
//	}
}
