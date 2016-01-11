package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.AuthorizationService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.TeamMateService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.WorkspaceService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils.ErrorCodes;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthorizationProvider;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceFileFollowService;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.service.shiro.ShiroRealm;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class WorkspaceBaseController extends BaseController {
	@Resource
	protected WorkspaceService workspaceService;
	@Resource
	protected AuthorizationService authorizationService;
	@Resource
	private ShiroRealm shiroRealm;
	@Resource
	protected TeamMateService teamMateService;
	@Resource
	protected SpaceService spaceService;
	@Resource
	protected SpaceFileFollowService spaceFileFollowService;
	@Resource
	protected ApplicationService applicationService;

	private ThreadLocal<String> role = new ThreadLocal<String>();
	
	protected void checkAccessible(SpaceFile file){
		if (file == null){
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		if (file.getModifiable()){
			if (Roles.SEPARATED.equals(currentRole())){
				if (!getUserPrincipal().getUserId().equals(file.getCreator().getId())){
					throw new RestException(ErrorCode.FILE_NOT_FOUND);
				}
			}
		}
	}
	
	protected void checkEditable(SpaceFile file){
		checkAccessible(file);
		if (!file.getModifiable()){
			throw new RestException(ErrorCode.FORBIDDEN, "file_not_modifiable");
		}
		if (Roles.READER.equals(currentRole()) || Roles.LIMITED_WRITER.equals(currentRole())){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
	}
	
	protected void checkDeletable(SpaceFile file){
		checkAccessible(file);
		if (!Roles.OWNER.equals(currentRole()) && !Roles.ADMIN.equals(currentRole())){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
	}

	// 用于转换SpaceFile到Dto
	protected Function<SpaceFile, SpaceFileDto> fileToDto = new Function<SpaceFile, SpaceFileDto>() {
		public SpaceFileDto apply(SpaceFile input) {
			SpaceFileDto output = SpaceFileDto.defaultTransformer.apply(input);
			if (Roles.compare(currentRole(), Roles.READER) >= 0) {
				output.favorite = spaceFileFollowService.findOne(currentUserId(), input.getId()) != null;
			}
			return output;
		}
	};

	protected Function<Workspace, WorkspaceDto> workspaceToDto = new Function<Workspace, WorkspaceDto>() {
		public WorkspaceDto apply(Workspace input) {
			Preconditions.checkNotNull(input);
			WorkspaceDto output = WorkspaceDto.WORKSPACE_TO_BRIEF.apply(input);
			output.role = workspaceService.getUserRole(input, currentUserId());
			return output;
		}
	};

	protected String currentRole() {
		return role.get();
	}

	@InitBinder({ "workspace", "file", "member" })
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("abcdefg");
	}

	/**
	 * 获取访问的工作空间
	 * 
	 * @param workspaceId
	 * @return
	 */
	protected void initWorkspace(Model model, long workspaceId) {
		if (!isAuthenticatedUser()) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		Workspace workspace = workspaceService.findWorkspace(workspaceId);
		if (workspace == null || !WorkspaceUtils.Status.ACITVE.equals(workspace.getStatus())){
			throw new RestException(ErrorCodes.WORKSPACE_NOT_FOUND);
		}
		UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
		role.set(workspaceService.getUserRole(workspace, principal.getUserId()));
		if (role.get() == null){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		AuthorizationProvider authorizationProvider = authorizationService.createAuthorizationProvider(workspace);
		shiroRealm.setAuthorizationProvider(authorizationProvider);

		model.addAttribute("workspace", workspace);
	}

	protected void initFile(Model model, String fileId) {
		Workspace workspace = getWorkspace(model);
		if (workspace.getSpace() == null) {
			throw new RestException(ErrorCodes.FILE_NOT_FOUND);
		}
		SpaceFile file = null;
		if ("root".equals(fileId)) {
			file = spaceService.findRoot(workspace.getSpace().getId());
		} else {
			file = spaceService.findFile(NumberUtils.toLong(fileId, -1));
		}
		if (file == null || !workspace.getSpace().getId().equals(file.getOwner().getId())) {
			throw new RestException(ErrorCodes.FILE_NOT_FOUND);
		}
		model.addAttribute("file", file);
	}

	protected void initTeamMate(Model model, long userId) {
		Workspace workspace = getWorkspace(model);
		if (workspace.getTeam() == null) {
			throw new RestException(ErrorCodes.TEAMMATE_NOT_FOUND);
		}
		TeamMate teamMate = workspaceService.findTeamMate(workspace.getTeam().getId(), userId);
		if (teamMate == null) {
			throw new RestException(ErrorCodes.TEAMMATE_NOT_FOUND);
		}
		model.addAttribute("member", teamMate);
	}

	protected Workspace getWorkspace(Model model) {
		return modelAttribute(model, "workspace");
	}

	protected SpaceFile getSpaceFileOrFolder(Model model) {
		return modelAttribute(model, "file");
	}

	protected SpaceFile getSpaceFolder(Model model) {
		SpaceFile file = getSpaceFileOrFolder(model);
		if (!file.getIsDir()) {
			throw new RestException(ErrorCodes.FOLDER_EXPECTED);
		}
		return file;
	}

	protected SpaceFile getSpaceFile(Model model) {
		SpaceFile file = getSpaceFileOrFolder(model);
		if (file.getIsDir()) {
			throw new RestException(ErrorCodes.FILE_EXPECTED);
		}
		return file;
	}

	protected TeamMate getMember(Model model) {
		return modelAttribute(model, "member");
	}

	@SuppressWarnings("unchecked")
	protected <T> T modelAttribute(Model model, String attributeName) {
		return (T) model.asMap().get(attributeName);
	}
}
