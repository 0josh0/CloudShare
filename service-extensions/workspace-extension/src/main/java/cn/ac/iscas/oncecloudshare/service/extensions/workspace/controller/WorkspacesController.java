package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.CreateWorkspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.AccessModifier;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.CreateWorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceJoinApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceSpace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils.ErrorCodes;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/api/v2/exts/workspaces", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class WorkspacesController extends WorkspaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(WorkspacesController.class);
	@Resource
	private ApplicationService applicationService;

	@ModelAttribute
	private void initModel() {
		if (!isAuthenticatedUser()) {
			throw new RestException(ErrorCode.UNAUTHORIZED);
		}
	}

	/**
	 * 申请创建工作空间
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String createWorkspace(@Valid CreateWorkspace request) {
		CreateWorkspaceApplication application = Application.defaultInit(new CreateWorkspaceApplication(), currentUser(), request);
		application = applicationService.save(application);
		applicationService.reviewApplication(application, new ReviewApplication(), currentUser());

		return gson().toJson(ApplicationDto.defaultInit(application, new ApplicationDto()));
	}

	/**
	 * 查找所有的public或protected的工作空间
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "joinable", method = RequestMethod.GET)
	@ResponseBody
	public String findJoinable(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("accessModifier", Operator.IN, new AccessModifier[] { AccessModifier.PROTECTED, AccessModifier.PUBLIC }));
		filters.add(new SearchFilter("status", Operator.EQ, WorkspaceUtils.Status.ACITVE));
		Page<Workspace> workspaces = workspaceService.findWorkspaces(filters, pageParam.getPageable(Workspace.class));
		// 查询用户在里面的角色
		return Gsons.filterByFields(WorkspaceDto.class, pageParam.getFields()).toJson(PageDto.of(workspaces, workspaceToDto));
	}

	/**
	 * 获取我加入的工作空间
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String findJoined(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("team.members.user", Operator.EQ, currentUser()));
		filters.add(new SearchFilter("status", Operator.EQ, WorkspaceUtils.Status.ACITVE));
		Page<Workspace> workspaces = workspaceService.findWorkspaces(filters, pageParam.getPageable(Workspace.class));
		return Gsons.filterByFields(WorkspaceDto.class, pageParam.getFields()).toJson(PageDto.of(workspaces, WorkspaceDto.WORKSPACE_TO_BRIEF));
	}

	/**
	 * 查看我的收藏
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "follows", method = RequestMethod.GET)
	@ResponseBody
	public String follows(PageParam pageParam) {
		Page<SpaceFileFollow> page = spaceFileFollowService.findAll(currentUserId(), WorkspaceSpace.class,
				pageParam.getPageable(SpaceFileFollow.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(page, SpaceFileDto.followTransformer));
	}
	
	/**
	 * 申请加入工作空间
	 * 
	 * @param request
	 * @param response
	 * @param apply
	 * @return
	 */
	@RequestMapping(value = "/{workspaceId:\\d+}/join", method = RequestMethod.POST)
	@ResponseBody
	public String joinWorkspace(@PathVariable("workspaceId") long workspaceId) {
		Workspace workspace = workspaceService.findWorkspace(workspaceId);
		if (workspace == null || !WorkspaceUtils.Status.ACITVE.equals(workspace.getStatus())){
			throw new RestException(ErrorCodes.WORKSPACE_NOT_FOUND);
		}
		// 判断是否已经是成员
		TeamMate mate = workspaceService.findTeamMate(workspace.getTeam().getId(), currentUserId());
		if (mate != null) {
			throw new RestException(ErrorCodes.DUPLICATE_JOIN);
		}
		// 判断工作空间的访问权限
		if (AccessModifier.PRIVATE.equals(workspace.getAccessModifier())) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		if (AccessModifier.PUBLIC.equals(workspace.getAccessModifier())) {
			mate = workspaceService.memeberJoined(workspace, currentUser());
			return ok();
		} else {
			// 查询用户是否已经申请加入了
			List<SearchFilter> filters = Lists.newArrayList();
			filters.add(new SearchFilter("workspace", Operator.EQ, workspace));
			filters.add(new SearchFilter("applyBy", Operator.EQ, currentUser()));
			filters.add(new SearchFilter("status", Operator.EQ, ApplicationStatus.TOREVIEW));
			WorkspaceJoinApplication application = applicationService.findApplication(WorkspaceJoinApplication.class, filters);
			if (application != null){
				application.setApplyAt(System.currentTimeMillis());
			} else {
				application = Application.defaultInit(new WorkspaceJoinApplication(), currentUser(), null);
				application.setWorkspace(workspace);
			}
			application = applicationService.save(application);
			return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
		}
	}
}