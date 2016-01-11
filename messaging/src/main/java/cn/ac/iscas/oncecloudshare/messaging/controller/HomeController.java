package cn.ac.iscas.oncecloudshare.messaging.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;

import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.ErrorResponseDto;

@Controller
public class HomeController extends BaseController {
	
	private static final JsonObject HOME_RESP=new JsonObject();
	
	static{
		HOME_RESP.addProperty("greeting","hello cloudshare messaging!");
	}
	
	@RequestMapping(value="/",method=RequestMethod.GET)
	@ResponseBody
	public String home(){
		return gson().toJson(HOME_RESP);
	}
	
	@RequestMapping(value="/error/400")
	public ResponseEntity<String> handle400(){
		ErrorResponseDto dto=new ErrorResponseDto(BasicErrorCode.BAD_REQUEST);
		return new ResponseEntity<String>(gson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/401")
	public ResponseEntity<String> handle401(){
		ErrorResponseDto dto=new ErrorResponseDto(BasicErrorCode.UNAUTHORIZED);
		return new ResponseEntity<String>(gson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/403")
	public ResponseEntity<String> handle403(){
		ErrorResponseDto dto=new ErrorResponseDto(BasicErrorCode.FORBIDDEN);
		return new ResponseEntity<String>(gson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/404")
	public ResponseEntity<String> handle404(){
		ErrorResponseDto dto=new ErrorResponseDto(BasicErrorCode.WRONG_API_URI);
		return new ResponseEntity<String>(gson().toJson(dto),
				dto.getHttpStatus());
	}
	
	@RequestMapping(value="/error/500")
	public ResponseEntity<String> handle500(){
		ErrorResponseDto dto=new ErrorResponseDto(BasicErrorCode.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<String>(gson().toJson(dto),
				dto.getHttpStatus());
	}
}
