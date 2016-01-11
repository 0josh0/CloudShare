package cn.ac.iscas.oncecloudshare.service.extensions.workspace.service;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.AdminApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationHandlerAdapter;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.dao.authorization.TeamMateDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.CreateWorkspaceApplicationDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.WorkspaceApplicationDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.WorkspaceDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.WorkspaceJoinApplicationDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao.WorkspaceTeamDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.CreateWorkspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.ReviewJoin;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto.InviteRequest;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto.UpdateRequest;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.ApplicationEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.MemberEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.CreateWorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceJoinApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceSpace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceTeam;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.common.BaseTeamService;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.system.CommonComponent;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class WorkspaceService extends CommonComponent{
	private static final Logger _logger = LoggerFactory.getLogger(WorkspaceService.class);

	@Resource
	private WorkspaceDao workspaceDao;
	@Resource
	private ApplicationService applicationService;
	@Resource
	private WorkspaceTeamDao workspaceTeamDao;
	@Resource
	private TeamMateDao teamMateDao;
	@Resource
	private AuthorizationService authorizationService;
	@Resource
	private SpaceService spaceService;
	@Resource
	private WorkspaceApplicationDao workspaceApplicationDao;
	@Resource
	private CreateWorkspaceApplicationDao createWorkspaceApplicationDao;
	@Resource
	private WorkspaceJoinApplicationDao workspaceJoinApplicationDao;
	@Resource
	private BaseTeamService baseTeamService;
	@Resource
	private UserService userService;

	@PostConstruct
	public void init() {
		applicationService.addApplicationDao(WorkspaceApplication.class, workspaceApplicationDao);
		// 创建工作申请
		applicationService.addApplicationDao(CreateWorkspaceApplication.class, createWorkspaceApplicationDao);
		applicationService.addApplicationHandler(new ApplicationHandlerAdapter() {			
			@Override
			public boolean canHandle(Application application) {
				return application instanceof CreateWorkspaceApplication;
			}

			@Override
			public void preReview(Application application, ReviewApplication review, User master) {
				if (review.getAgreed()) {
					CreateWorkspaceApplication _application = (CreateWorkspaceApplication)application;
					createWorkspaceAgreed(_application, review);
				}
			}
		});
		// 加入工作空间申请
		applicationService.addApplicationDao(WorkspaceJoinApplication.class, workspaceJoinApplicationDao);
		applicationService.addApplicationHandler(new ApplicationHandlerAdapter() {
			@Override
			public boolean canHandle(Application application) {
				return application instanceof WorkspaceJoinApplication;
			}

			@Override
			public void preReview(Application application, ReviewApplication review, User master) {
				if (review.getAgreed()) {
					WorkspaceJoinApplication _application = (WorkspaceJoinApplication) application;
					// 查询用户是否已经加入
					TeamMate member = findTeamMate(_application.getWorkspace().getTeam().getId(), application.getApplyBy().getId());
					if (member == null) {
						member = memeberJoined(_application.getWorkspace(), application.getApplyBy(), ((ReviewJoin) review).getRole());
						if (member != null) {
							postEvent(new MemberEvent(getUserPrincipal(), _application.getWorkspace(), member, MemberEvent.EVENT_JOINED));
						}
					}
				}
			}
		});
		
		applicationService.addApplicationHandler(new ApplicationHandlerAdapter() {
			@Override
			public boolean canHandle(Application application) {
				return application instanceof WorkspaceApplication;
			}

			@Override
			public void postSave(Application application) {
				WorkspaceApplication wsapp = (WorkspaceApplication) application;
				postEvent(new ApplicationEvent(getUserPrincipal(), wsapp.getWorkspace(), ApplicationEvent.EVENT_CREATED, wsapp));
			}
			
			@Override
			public void postReview(Application application, ReviewApplication review) {
				WorkspaceApplication wsapp = (WorkspaceApplication) application;
				postEvent(new ApplicationEvent(getUserPrincipal(), wsapp.getWorkspace(), ApplicationEvent.EVENT_REVIEWED, wsapp));
			}
		});
	}

	public Workspace findWorkspace(long id) {
		return workspaceDao.findOne(id);
	}

	/**
	 * 查找用户加入的工作空间
	 * 
	 * @param userId
	 * @param status
	 * @param pageable
	 * @return
	 */
	public Page<Workspace> findJoined(long userId, String status, Pageable pageable) {
		return workspaceDao.findJoined(userId, status, pageable);
	}

	public Page<Workspace> listWorkspacesByTeamId(List<Long> teamIds, Pageable pageable) {
		return workspaceDao.listByTeamIds(teamIds, pageable);
	}

	public Iterable<TeamMate> listTeamMatesByUser(long userId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("user.id", Operator.EQ, userId));
		filters.add(new SearchFilter("team.type", Operator.EQ, WorkspaceUtils.DOMAIN));
		return teamMateDao.findAll(Specifications.fromFilters(filters, TeamMate.class));
	}

	public Page<Workspace> findWorkspaces(Collection<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<Workspace> spec = Specifications.fromFilters(filters, Workspace.class);
			return workspaceDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 更新workspace
	 * 
	 * @param workspace
	 * @param request
	 */
	@Transactional(readOnly = false)
	public void update(Workspace workspace, UpdateRequest request) {
		workspace.setName(request.getName());
		workspace.setDescription(request.getDescription());
		workspace.setAccessModifier(request.getAccessModifier());
		workspace.setDefaultMemberRole(request.getDefaultMemberRole());
		workspaceDao.save(workspace);
	}

	/**
	 * 邀请成员
	 * 
	 * @param workspace
	 * @param inviteRequest
	 * @return
	 */
	@Transactional(readOnly = false)
	public List<TeamMate> invite(Workspace workspace, InviteRequest inviteRequest) {
		List<TeamMate> toAdd = Lists.newArrayList();
		for (long userId : inviteRequest.getTargets()) {
			if (workspace.getTeam().hasMember(userId)){
				continue;
			}
			User user = userService.find(userId);
			if (user == null) {
				continue;
			}
			TeamMate teamMate = new TeamMate();
			teamMate.setTeam(workspace.getTeam());
			teamMate.setUser(user);
			teamMate.setJoinTime(new Date());
			int index = StringUtils.isEmpty(inviteRequest.getRole()) ? -1 : Roles.ALL.indexOf(inviteRequest.getRole());
			teamMate.setRole(index == -1 ? workspace.getDefaultMemberRole() : inviteRequest.getRole());
			toAdd.add(teamMate);
		}
		if (toAdd.size() > 0){
			return baseTeamService.addMembers(workspace.getTeam(), toAdd);
		}
		return Lists.newArrayList();
	}

	@Transactional(readOnly = false)
	public TeamMate memeberJoined(Workspace workspace, User user) {
		return memeberJoined(workspace, user, workspace.getDefaultMemberRole());
	}	

	@Transactional(readOnly = false)
	private TeamMate memeberJoined(Workspace workspace, User user, String role) {
		TeamMate teamMate = new TeamMate();
		teamMate.setTeam(workspace.getTeam());
		teamMate.setUser(user);
		teamMate.setJoinTime(new Date());
		teamMate.setRole(Roles.has(role) ? role : workspace.getDefaultMemberRole());
		List<TeamMate> members = baseTeamService.addMembers(workspace.getTeam(), Lists.newArrayList(teamMate));
		if (members.size() == 1){
			return members.get(0);
		}
		return null;
	}

	@Transactional(readOnly = false)
	public void createWorkspaceAgreed(AdminApplication application, ReviewApplication review) {
		CreateWorkspace reviewContent = application.getContentObject(CreateWorkspace.class);
		// 创建小组
		WorkspaceTeam team = new WorkspaceTeam();
		team.setDescription("创建工作空间(" + reviewContent.getName() + ")时自动创建");
		team.setMembersCount(1);
		baseTeamService.createTeam(team, reviewContent.getName(), application.getApplyBy());
		// 创建空间
		WorkspaceSpace space = new WorkspaceSpace();
		space.setFilesCount(0L);
		space.setQuota(reviewContent.getQuota());
		space.setRestQuota(reviewContent.getQuota());
		spaceService.save(space);
		// 创建对应的默认文件夹
		spaceService.makeBuildinFolders(space);
		// 创建workspace对象
		Workspace workspace = new Workspace();
		workspace.setTeam(team);
		workspace.setSpace(space);
		workspace.setName(reviewContent.getName());
		workspace.setApplyTime(new Date(application.getApplyAt()));
		workspace.setDescription(reviewContent.getDescription());
		workspace.setStatus(WorkspaceUtils.Status.ACITVE);
		workspace.setApplyBy(application.getApplyBy());
		workspace.setOwner(application.getApplyBy());
		workspace.setAccessModifier(reviewContent.getAccessModifier());
		workspace.setDefaultMemberRole(reviewContent.getDefaultRole());
		workspace = workspaceDao.save(workspace);
	}

	public TeamMate findTeamMate(long teamId, long userId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("team.id", Operator.EQ, teamId));
		filters.add(new SearchFilter("user.id", Operator.EQ, userId));
		Iterable<TeamMate> iterable = findTeamMates(filters);
		if (iterable == null) {
			return null;
		}
		Iterator<TeamMate> iterator = iterable.iterator();
		TeamMate result = null;
		if (iterator.hasNext()) {
			result = iterator.next();
		}
		if (iterator.hasNext()) {
			_logger.warn("Team#{} User#{}对应有多个TeamMate对象", teamId, userId);
		}
		return result;
	}

	/**
	 * 获取用户在工作空间中的角色
	 * 
	 * @param workspace
	 * @param userId
	 * @return
	 */
	public String getUserRole(Workspace workspace, Long userId) {
		if (workspace.getTeam() == null || userId == null) {
			// return Roles.ANON;
			return null;
		}
		TeamMate teamMate = findTeamMate(workspace.getTeam().getId(), userId);
		if (teamMate == null) {
			// return Roles.USER;
			return null;
		}
		if (StringUtils.isEmpty(teamMate.getRole())) {
			if (workspace.getDefaultMemberRole() == null){
				return Roles.READER;
			}
			return workspace.getDefaultMemberRole();
		}
		return teamMate.getRole();
	}

	/**
	 * 列出所有的工作组成员
	 * 
	 * @param filters
	 * @param pageable
	 * @return
	 */
	public Iterable<TeamMate> findTeamMates(List<SearchFilter> filters) {
		try {
			Specification<TeamMate> spec = Specifications.fromFilters(filters, TeamMate.class);
			return teamMateDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 解散工作空间
	 *
	 * @param workspace
	 */
	@Transactional(readOnly = false)
	public void dismiss(Workspace workspace) {
		workspace.setStatus(WorkspaceUtils.Status.DISMISSED);
		workspaceDao.save(workspace);
	}
}
