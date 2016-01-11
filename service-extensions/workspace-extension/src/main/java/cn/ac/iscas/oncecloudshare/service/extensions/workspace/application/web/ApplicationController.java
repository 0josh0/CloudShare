package cn.ac.iscas.oncecloudshare.service.extensions.workspace.application.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.model.AdminApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/{ver}/applications/{id:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ApplicationController extends BaseController {
	@Resource
	private ApplicationService applicationService;

	/**
	 * 获取访问的申请
	 * 
	 * @param applicationId
	 * @return
	 */
	@ModelAttribute("application")
	public Application initApplication(@PathVariable("id") long applicationId) {
		Application application = applicationService.findOne(AdminApplication.class, applicationId);
		if (application == null) {
			throw new RestException(ErrorCode.APPLICATION_NOT_FOUND);
		}
		if (!application.getApplyBy().equals(currentUser())) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		return application;
	}

	/**
	 * 取消我的申请
	 * 
	 * @param query
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(params = { "cancel" }, method = RequestMethod.PUT)
	@ResponseBody
	public String cancelApplication(@ModelAttribute("application") Application application) {
		if (!application.getStatus().equals(ApplicationStatus.TOREVIEW)) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		applicationService.cancelApplication(application);
		return ok();
	}
}
