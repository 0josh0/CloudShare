package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto.UpdateMemberRequest;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.MemberChangeRoleEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.MemberEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller("workspaceMemberController")
@RequestMapping(value = "/api/v2/exts/workspaces/{workspaceId:\\d+}/members/{userId:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8,
		MediaTypes.JSON_UTF8 })
public class TeamMateController extends WorkspaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(TeamMateController.class);

	@ModelAttribute
	public void initModel(Model model, @PathVariable long workspaceId, @PathVariable long userId) {
		initWorkspace(model, workspaceId);
		initTeamMate(model, userId);
	}

	@RequestMapping(params = "!role", method = RequestMethod.PUT)
	@ResponseBody
	public String update(@ModelAttribute("member") TeamMate member, UpdateMemberRequest updateMemberRequest) {
		checkPermission(member, "edit");
		member = teamMateService.update(member, updateMemberRequest);
		WorkspaceDto.Member dto = new WorkspaceDto.Member(member);
		dto.permissions = authorizationService.getPermissions(currentUserId(), currentRole(), member);
		return gson().toJson(dto);
	}

	@RequestMapping(params = "role", method = RequestMethod.PUT)
	@ResponseBody
	public String updateRole(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("member") TeamMate member,
			@RequestParam("role") String role) {
		checkPermission(member, "changeRole");
		List<String> limitRoles = authorizationService.getChangedToRoles(currentRole());
		if (limitRoles.indexOf(role) == -1) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		member = teamMateService.updateRole(member, role);
		WorkspaceDto.Member dto = new WorkspaceDto.Member(member);
		dto.permissions = authorizationService.getPermissions(currentUserId(), currentRole(), member);

		postEvent(new MemberChangeRoleEvent(getUserPrincipal(), workspace, member, role));

		return gson().toJson(dto);
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public String kick(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("member") TeamMate member) {
		checkPermission(member, "kick");
		teamMateService.delete(member);
		postEvent(new MemberEvent(getUserPrincipal(), workspace, member, MemberEvent.EVENT_KICK));
		return gson().toJson(ResponseDto.OK);
	}

	protected void checkPermission(TeamMate member, String operation) {
		List<String> permissions = authorizationService.getPermissions(currentUserId(), currentRole(), member);
		if (permissions.indexOf(operation) == -1) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
	}
}