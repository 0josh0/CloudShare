package cn.ac.iscas.oncecloudshare.service.dto;

import org.springframework.http.HttpStatus;

import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

public class ResponseDto {

	public static ResponseDto OK=new ResponseDto(HttpStatus.OK,"OK");

	@GsonHidden
	protected HttpStatus httpStatus;
	protected int statusCode;
	protected String message;

	public ResponseDto(){
	}
	
	public ResponseDto(HttpStatus httpStatus, String message){
		this.httpStatus=httpStatus;
		this.statusCode=httpStatus.value();
		this.message=message;
	}

	public HttpStatus getHttpStatus(){
		return httpStatus;
	}

	public String getMessage(){
		return message;
	}

}
