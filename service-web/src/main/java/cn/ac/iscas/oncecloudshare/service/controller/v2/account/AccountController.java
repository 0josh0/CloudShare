package cn.ac.iscas.oncecloudshare.service.controller.v2.account;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.annotation.AnonApi;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.account.PasswordResetInfo;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.TempItem;
import cn.ac.iscas.oncecloudshare.service.service.account.PasswordResetService;
import cn.ac.iscas.oncecloudshare.service.service.common.TempItemService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.GlobalUserService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/v2/account", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AccountController extends BaseController {

	@Autowired
	PasswordResetService prService;
	
	@Autowired
	TempItemService tiService;
	@Resource
	private GlobalUserService globalUserService;
	@Resource
	private TenantService tenantService;

	private User findUser(String email) {
		User user = uService.findByEmail(email);
		if (user == null) {
			throw new RestException(ErrorCode.USER_NOT_FOUND);
		}
		return user;
	}

	private PasswordResetInfo findPasswordResetInfo(String token) {
		PasswordResetInfo info = prService.find(token);
		if (info == null) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		return info;
	}
	
	private TempItem findTempItem(String key){
		TempItem ti=tiService.find(key);
		if(ti==null){
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		return ti;
	}
	

	@AnonApi
	@RequestMapping(value = "requestPasswordReset", method = RequestMethod.POST)
	@ResponseBody
	public String requestPasswordReset(@RequestParam String email) {
		Long tenantId = globalUserService.findTenantId(email);
		if (tenantId == null){
			throw new RestException(ErrorCode.USER_NOT_FOUND);
		}
		tenantService.setCurrentTenantManually(tenantId);
		User user = findUser(email);
		return gson().toJson(prService.requestPaswordReset(user));
	}

	@AnonApi
	@RequestMapping(value = "passwordResetInfo", method = RequestMethod.GET)
	@ResponseBody
	public String getPasswordInfo(@RequestParam String token) {
		return gson().toJson(findPasswordResetInfo(token));
	}

	@AnonApi
	@RequestMapping(value = "resetPassword", method = RequestMethod.PUT)
	@ResponseBody
	public String requestPasswordReset(@RequestParam String token, @RequestParam String newPassword) {
		PasswordResetInfo info = findPasswordResetInfo(token);
		User user = findUser(info.email);
		uService.changePassword(user.getId(), newPassword);
		return ok();
	}

	@AnonApi
	@RequestMapping(value = "activate", method = RequestMethod.PUT)
	@ResponseBody
	public String activate(@RequestParam String token, @RequestParam String password, @RequestParam(required = false) String name) {
		TempItem ti = findTempItem(token);
		User user = findUser(ti.getContent());
		uService.activate(user.getId(), password, name);
		tiService.delete(token);
		return ok();
	}
	

}
