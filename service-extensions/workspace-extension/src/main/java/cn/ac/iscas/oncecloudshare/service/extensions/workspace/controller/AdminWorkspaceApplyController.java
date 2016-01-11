package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InsufficientQuotaException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.ReviewJoin;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.ReviewUpload;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils.ApplicationTypes;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/{apiVer}/exts/workspaces/{wsId}/admin/applys/{applyId}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AdminWorkspaceApplyController extends WorkspaceBaseController {
	@Resource
	private ApplicationService applicationService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long wsId, @PathVariable long applyId) {
		initWorkspace(model, wsId);
		
		// 初始化申请
		Application application = applicationService.findOne(WorkspaceApplication.class, applyId);
		if (application == null || !(application instanceof WorkspaceApplication)) {
			throw new RestException(ErrorCode.APPLICATION_NOT_FOUND);
		}
		WorkspaceApplication wsApplication = (WorkspaceApplication) application;
		if (!wsApplication.getWorkspace().equals(getWorkspace(model))) {
			throw new RestException(ErrorCode.APPLICATION_NOT_FOUND);
		}
		model.addAttribute("application", application);
	}
	
	/**
	 * 审核上传文件申请
	 * 
	 * @param applicationId
	 * @param reviewRequest
	 * @return
	 */
	@RequiresRoles(value = { Roles.OWNER, Roles.ADMIN }, logical = Logical.OR)
	@RequestMapping(value = "/review", params={"type=" + ApplicationTypes.UPLOAD}, method = RequestMethod.PUT)
	@ResponseBody
	public String reviewUpload(@ModelAttribute("application")WorkspaceApplication application, @Valid ReviewUpload review) {
		applicationService.reviewApplication(application, review, currentUser());
		return ok();
	}

	/**
	 * 审核上传新版本申请
	 * 
	 * @param applicationId
	 * @param reviewRequest
	 * @return
	 * @throws IOException
	 * @throws InsufficientQuotaException
	 */
	@RequiresRoles(value = { Roles.OWNER, Roles.ADMIN }, logical = Logical.OR)
	@RequestMapping(value = "/review", params={"type=" + ApplicationTypes.UPLOAD_VERSION}, method = RequestMethod.PUT)
	@ResponseBody
	public String reviewUploadVersion(@ModelAttribute("application")WorkspaceApplication application, @Valid ReviewApplication review) {
		applicationService.reviewApplication(application, review, currentUser());
		return ok();
	}
	
	/**
	 * 审核加入申请
	 *
	 * @param application
	 * @param review
	 * @return
	 */
	@RequiresRoles(value = { Roles.OWNER, Roles.ADMIN }, logical = Logical.OR)
	@RequestMapping(value = "/review", params={"type=" + ApplicationTypes.JOIN}, method = RequestMethod.PUT)
	@ResponseBody
	public String reviewJoin(@ModelAttribute("application")WorkspaceApplication application, @Valid ReviewJoin review){
		applicationService.reviewApplication(application, review, currentUser());
		return ok();
	}
}
