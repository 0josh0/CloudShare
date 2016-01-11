package cn.ac.iscas.oncecloudshare.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorResponseDto;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

@Controller
public class HomeController {
	
	@Autowired
	RuntimeContext runtimeContext;
	
	@RequestMapping(value="/",method=RequestMethod.GET)
	@ResponseBody
	public String home(){
		return "hello cloudshare!";
	}
	
	@RequestMapping(value="/error/400")
	public ResponseEntity<String> handle400(){
		ErrorResponseDto dto=new ErrorResponseDto(ErrorCode.BAD_REQUEST);
		return new ResponseEntity<String>(Gsons.defaultGson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/401")
	public ResponseEntity<String> handle401(){
		ErrorResponseDto dto=new ErrorResponseDto(ErrorCode.UNAUTHORIZED);
		return new ResponseEntity<String>(Gsons.defaultGson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/403")
	public ResponseEntity<String> handle403(){
		ErrorResponseDto dto=new ErrorResponseDto(ErrorCode.FORBIDDEN);
		return new ResponseEntity<String>(Gsons.defaultGson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/404")
	public ResponseEntity<String> handle404(){
		ErrorResponseDto dto=new ErrorResponseDto(ErrorCode.WRONG_API_URI);
		return new ResponseEntity<String>(Gsons.defaultGson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/500")
	public ResponseEntity<String> handle500(){
		ErrorResponseDto dto=new ErrorResponseDto(ErrorCode.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<String>(Gsons.defaultGson().toJson(dto),
				dto.getHttpStatus());
	}
}
