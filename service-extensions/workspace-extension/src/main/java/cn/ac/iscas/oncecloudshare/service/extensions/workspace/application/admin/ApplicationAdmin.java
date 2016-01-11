package cn.ac.iscas.oncecloudshare.service.extensions.workspace.application.admin;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.AdminApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/adminapi/v2/applys/{id:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ApplicationAdmin extends BaseController{
	@Resource
	private ApplicationService applicationService;
	
	/**
	 * 获取访问的申请
	 * 
	 * @param workspaceId
	 * @return
	 */
	@ModelAttribute("application")
	public Application initApplication(@PathVariable("id") long applicationId) {
		Application application = applicationService.findOne(AdminApplication.class, applicationId);
		if (application == null){
			throw new RestException(ErrorCode.APPLICATION_NOT_FOUND);
		}
		return application;
	}
	
	/**
	 * 审核申请
	 * 
	 * @param application 申请
	 * @param reviewRequest 审核意见
	 * @return
	 */
	@RequestMapping(value = "review", method = RequestMethod.PUT)
	@ResponseBody
	public String review(@ModelAttribute("application") AdminApplication application, @Valid ReviewApplication request) {
		applicationService.reviewApplication(application, request, currentUser());
		return ok();
	}
}
