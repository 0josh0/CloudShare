package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceJoinApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

@Controller
@RequestMapping(value = "/api/{apiVer}/exts/workspaces/{wsId}/admin/applys", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AdminWorkspaceApplysController extends WorkspaceBaseController {
	@Resource
	private ApplicationService applicationService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long wsId) {
		initWorkspace(model, wsId);
	}

	/**
	 * 查询我的申请记录
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequiresRoles(value = { Roles.OWNER, Roles.ADMIN }, logical = Logical.OR)
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String listAdminApplications(@ModelAttribute("workspace") Workspace workspace, @RequestParam(required = false) String q,
			@RequestParam(required = false) String type, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		Class<? extends WorkspaceApplication> clazz = WorkspaceApplication.class;
		if (WorkspaceUtils.ApplicationTypes.JOIN.equals(type)) {
			clazz = WorkspaceJoinApplication.class;
		} else if (WorkspaceUtils.ApplicationTypes.UPLOAD.equals(type)) {
			clazz = WorkspaceUploadApplication.class;
		} else if (WorkspaceUtils.ApplicationTypes.UPLOAD_VERSION.equals(type)) {
			clazz = WorkspaceUploadVersionApplication.class;
		}
		filters.add(new SearchFilter("workspace", Operator.EQ, workspace));
		Page page = applicationService.findApplications(clazz, filters, pageParam.getPageable(clazz));
		return Gsons.filterByFields(WorkspaceApplicationDto.class, pageParam.getFields()).toJson(
				PageDto.of(page, WorkspaceApplicationDto.defaultTransformer));
	}
}
