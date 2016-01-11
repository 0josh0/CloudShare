package cn.ac.iscas.oncecloudshare.service.controller.v2.global;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.GlobalUserService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/globalapi/v2/users", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class GlobalUserController extends BaseController{
	@Resource
	private GlobalUserService globalUserService;
	
	@RequestMapping(value="checkEmail", method=RequestMethod.POST)
	@ResponseBody
	public String checkEmail(@RequestParam("email") String email){
		Long tenantId = globalUserService.findTenantId(email);
		if (tenantId != null){
			throw new RestException(ErrorCode.CONFLICT, "login_email_exsits");
		}
		return gson().toJson(ResponseDto.OK);
	}
}