package cn.ac.iscas.oncecloudshare.service.controller.v2;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.gson.Gson;

public class BaseController {

	protected Gson gson=Gsons.defaultGson();
	
	@Autowired
	protected ConfigService cService;
	
	protected Gson gson(){
		return Gsons.defaultGson();
	}
	
	/**
	 * 正确返回
	 * @return
	 */
	protected String ok(){
		return gson().toJson(ResponseDto.OK);
	}
	
	/**
	 * 当前接口调用者的身份
	 * @return
	 */
	protected Object getPrincipal(){
		return SecurityUtils.getSubject().getPrincipal();
	}
	
	protected HttpServletRequest getRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
	}
}
