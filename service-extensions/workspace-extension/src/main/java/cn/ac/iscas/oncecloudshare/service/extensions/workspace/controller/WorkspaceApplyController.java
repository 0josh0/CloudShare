package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/{ver}/exts/workspaces/applys/{applyId}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class WorkspaceApplyController extends WorkspaceBaseController {
	@Resource
	private ApplicationService applicationService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long applyId) {
		// 初始化申请
		WorkspaceApplication application = applicationService.findOne(WorkspaceApplication.class, applyId);
		if (application == null || !application.getApplyBy().equals(currentUser())) {
			throw new RestException(ErrorCode.APPLICATION_NOT_FOUND);
		}
		model.addAttribute("application", application);
	}

	/**
	 * 撤销申请
	 * 
	 * @param application
	 */
	@RequestMapping(value = "/cancel", method = RequestMethod.PUT)
	@ResponseBody
	public String cancel(@ModelAttribute("application") WorkspaceApplication application) {
		if (ApplicationStatus.TOREVIEW.equals(application.getStatus())){
			applicationService.cancelApplication(application);
		}
		return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
	}
}