package cn.ac.iscas.oncecloudshare.messaging.controller;

import java.io.IOException;

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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.ErrorResponseDto;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.BusinessException;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.RestServerException;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;

import com.google.common.base.Strings;
import com.google.gson.Gson;


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
	
//	/**
//	 * handle JSR311 Validation
//	 */
//	@ExceptionHandler(value={ConstraintViolationException.class})
//	public final ResponseEntity<?> handleException(
//			ConstraintViolationException ex,WebRequest request){
//		// Map<String, String> errors =
//		// BeanValidators.extractPropertyAndMessage(ex.getConstraintViolations());
//		// String body = jsonMapper.toJson(errors);
//		String body="validation error";
//		return handleExceptionInternal(ex,body,getHttpHeaders(),
//				HttpStatus.BAD_REQUEST,request);
//	}
	
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
	
	@ExceptionHandler(value={BusinessException.class})
	public final ResponseEntity<?> handleException(
			BusinessException ex,WebRequest request){
		String message=ex.getMessage();
		if(Strings.isNullOrEmpty(message)){
			message=ex.getErrorCode().getMessage();
		}
		ErrorResponseDto errorDto=new ErrorResponseDto(ex.getErrorCode(),message);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}


	/**
	 * AuthorizationException 权限异常
	 */
	@ExceptionHandler(value=AuthorizationException.class)
	public final ResponseEntity<?> handleException(
			AuthorizationException ex,WebRequest request){
		ErrorResponseDto errorDto=new ErrorResponseDto(BasicErrorCode.FORBIDDEN);
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
	 * handle io exceptions
	 */
	@ExceptionHandler(value=IOException.class)
	public final ResponseEntity<Object> handleException(
			IOException ex,WebRequest request){
		logger.error("IO Exception: ",ex);
		ErrorResponseDto errorDto=new ErrorResponseDto(BasicErrorCode.INTERNAL_SERVER_ERROR);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
	
	/**
	 * handle rest server exceptions
	 */
	@ExceptionHandler(value=RestServerException.class)
	public final ResponseEntity<Object> handleException(
			RestServerException ex,WebRequest request){
		logger.error("RestServerException: ",ex);
		ErrorResponseDto errorDto=new ErrorResponseDto(BasicErrorCode.INTERNAL_SERVER_ERROR);
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
		ErrorResponseDto errorDto=new ErrorResponseDto(BasicErrorCode.INTERNAL_SERVER_ERROR);
		return handleExceptionInternal(ex,gson.toJson(errorDto),createHeaders(),
				errorDto.getHttpStatus(),request);
	}
}
