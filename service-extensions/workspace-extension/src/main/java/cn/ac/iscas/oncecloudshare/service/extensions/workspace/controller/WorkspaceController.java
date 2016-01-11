package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.TeamMateDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto.InviteRequest;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto.UpdateRequest;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.MemberEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.AuthorizationService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/api/v2/exts/workspaces/{workspaceId:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class WorkspaceController extends WorkspaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(WorkspaceController.class);

	@Resource
	private AuthorizationService authorizationService;
	@Resource
	private ApplicationService applicationService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long workspaceId) {
		initWorkspace(model, workspaceId);
	}

	@RequiresPermissions("workspace:view")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String view(@ModelAttribute("workspace") Workspace workspace) {
		WorkspaceDto dto = WorkspaceDto.WORKSPACE_TO_DETAIL.apply(workspace);
		dto.role = currentRole();
		dto.permissions = authorizationService.getPermissions(currentUserId(), currentRole(), workspace);
		return gson().toJson(dto);
	}

	@RequiresRoles({ Roles.OWNER })
	@RequestMapping(method = RequestMethod.PUT)
	@ResponseBody
	public String update(@ModelAttribute("workspace") Workspace workspace, UpdateRequest request) {
		workspaceService.update(workspace, request);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresRoles(value = { Roles.OWNER, Roles.ADMIN }, logical = Logical.OR)
	@RequestMapping(value = "invite", method = RequestMethod.PUT)
	@ResponseBody
	public String invite(@ModelAttribute("workspace") Workspace workspace, InviteRequest inviteRequest) {
		List<TeamMate> addedMembers = workspaceService.invite(workspace, inviteRequest);
		// 发送消息
		if (addedMembers != null && addedMembers.size() > 0){
			postEvent(new MemberEvent(getUserPrincipal(), workspace, addedMembers, MemberEvent.EVENT_JOINED));
		}
		return gson.toJson(Lists.transform(addedMembers, TeamMateDto.DEFAULT_TRANSFORMER));
	}

	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "members", method = RequestMethod.GET)
	@ResponseBody
	public String listMembers(@ModelAttribute("workspace") Workspace workspace) {
		List<TeamMate> teamMates = workspace.getTeam().getMembers();
		List<WorkspaceDto.Member> results = Lists.newArrayList();
		for (TeamMate teamMate : teamMates) {
			WorkspaceDto.Member member = new WorkspaceDto.Member(teamMate);
			member.permissions = authorizationService.getPermissions(currentUserId(), currentRole(), teamMate);
			results.add(member);
		}
		return gson().toJson(results);
	}
	
	@RequiresRoles({ Roles.OWNER })
	@RequestMapping(value = "dismiss", method = RequestMethod.PUT)
	@ResponseBody
	public String dismiss(@ModelAttribute("workspace") Workspace workspace){
		workspaceService.dismiss(workspace);
		return gson().toJson(ResponseDto.OK);
	}
}