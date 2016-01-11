package cn.ac.iscas.oncecloudshare.service.controller.v2;

import java.io.IOException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorResponseDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.BusinessException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;


@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static Logger logger=LoggerFactory.getLogger(ControllerExceptionHandler.class);
	
	Gson gson=Gsons.defaultGson();

	private HttpHeaders createHeaders(){
		HttpHeaders headers=new HttpHeaders();
		headers.setContentType(MediaType
				.parseMediaType(MediaTypes.TEXT_PLAIN_UTF8));
		return headers;
	}
	
	@ExceptionHandler(value={BusinessException.class})
	public final ResponseEntity<?> handleException(
			BusinessException ex,WebRequest request){
		String message=ex.getMessage();
		if(Strings.isNullOrEmpty(message)){
			message=ex.getErrorCode().message;
		}
		ErrorResponseDto errorDto=new ErrorResponseDto(ex.getErrorCode(),message);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
	
	/**
	 * handle JSR311 Validation
	 */
	@ExceptionHandler(value={ConstraintViolationException.class})
	public final ResponseEntity<?> handleException(
			ConstraintViolationException ex,WebRequest request){
		StringBuilder sb=new StringBuilder();
		for(ConstraintViolation<?> v:ex.getConstraintViolations()){
			sb.append(v.getPropertyPath()+":"+v.getMessage()+", ");
		}
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.INVALID_PARAM,
				sb.toString());
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				HttpStatus.BAD_REQUEST,request);
	}
	
	/**
	 * 数据库冲突异常
	 * @param ex
	 * @param request
	 * @return
	 */
	@ExceptionHandler(value={MySQLIntegrityConstraintViolationException.class})
	public final ResponseEntity<?> handleException(
			MySQLIntegrityConstraintViolationException ex,WebRequest request){
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.CONFLICT);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				HttpStatus.BAD_REQUEST,request);
	}
	
//	/**
//	 * handle PropertyReferenceException 
//	 * (usually caused by invalid 'fields' parameter)
//	 */
//	@ExceptionHandler(value=PropertyReferenceException.class)
//	public final ResponseEntity<?> handleException(
//			PropertyReferenceException ex, WebRequest request){
//		ErrorResponseDto errorDto=new ErrorResponseDto(HttpStatus.BAD_REQUEST,
//				"invalid fileds parameter");
//		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
//				HttpStatus.BAD_REQUEST,request);
//	}

	
	/**
	 * AuthorizationException 权限异常
	 */
	@ExceptionHandler(value=AuthorizationException.class)
	public final ResponseEntity<?> handleException(
			AuthorizationException ex,WebRequest request){
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.FORBIDDEN);
		return handleExceptionInternal(ex,gson.toJson(errorDto),
				createHeaders(),errorDto.getHttpStatus(),request);
	}

	/**
	 * handle RestException
	 */
	@ExceptionHandler(value=RestException.class)
	public final ResponseEntity<?> handleException(
			RestException ex,WebRequest request){
		return handleExceptionInternal(ex,gson.toJson(ex.errorResponseDto),
				createHeaders(),ex.getHttpStatus(),request);
	}
	
	/**
	 * handle IllegalArgumentException
	 */
	@ExceptionHandler(value=IllegalArgumentException.class)
	public final ResponseEntity<?> handleException(
			IllegalArgumentException ex,WebRequest request){
		ErrorResponseDto errorDto=new ErrorResponseDto(HttpStatus.BAD_REQUEST,
				ex.getLocalizedMessage());
		return handleExceptionInternal(ex,gson.toJson(errorDto),
				createHeaders(),errorDto.getHttpStatus(),request);
	}
	
	/**
	 * MaxUploadSizeExceededException，上传文件太大
	 */
	@ExceptionHandler(value=MaxUploadSizeExceededException.class)
	public final ResponseEntity<Object> handleException(
			MaxUploadSizeExceededException ex,WebRequest request){
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.BAD_REQUEST,
				"max upload size exceeded");
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
	
	/**
	 * MultipartException，一般是上传文件太大或上传中断引起，不做处理
	 */
	@ExceptionHandler(value=MultipartException.class)
	public final ResponseEntity<Object> handleException(
			MultipartException ex,WebRequest request){
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.BAD_REQUEST,
				"MultipartException");
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
	
	
	/**
	 * handle io exceptions
	 */
	@ExceptionHandler(value=IOException.class)
	public final ResponseEntity<Object> handleException(
			IOException ex,WebRequest request){
		logger.error("IO Exception: ",ex);
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.INTERNAL_SERVER_ERROR);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
	
	/**
	 * handle unrecognized exceptions
	 */
	@ExceptionHandler(value=RuntimeException.class)
	public final ResponseEntity<Object> handleException(
			RuntimeException ex,WebRequest request){
		logger.error("Unhandled error: ",ex);
		ErrorResponseDto errorDto=new ErrorResponseDto(ErrorCode.INTERNAL_SERVER_ERROR);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
}
