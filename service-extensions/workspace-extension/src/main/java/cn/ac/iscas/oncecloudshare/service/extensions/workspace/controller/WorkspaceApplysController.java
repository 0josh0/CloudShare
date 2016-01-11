package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

@Controller
@RequestMapping(value = "/api/{apiVer}/exts/workspaces/applys", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class WorkspaceApplysController extends WorkspaceBaseController {
	@Resource
	private ApplicationService applicationService;
	
	/**
	 * 查询我的申请记录
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String findPage(@RequestParam(required = false) String q, PageParam pageParam){
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("applyBy", Operator.EQ, currentUser()));
		Page<WorkspaceApplication> page = applicationService.findApplications(WorkspaceApplication.class, filters,
				pageParam.getPageable(WorkspaceApplication.class));
		return Gsons.filterByFields(WorkspaceApplicationDto.class, pageParam.getFields()).toJson(
				PageDto.of(page, WorkspaceApplicationDto.defaultTransformer));
	}
}