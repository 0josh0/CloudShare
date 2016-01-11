package cn.ac.iscas.oncecloudshare.service.extensions.workspace.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.TeamMateDao;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto.UpdateMemberRequest;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.common.BaseTeamService;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class TeamMateService {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(TeamMateService.class);

	@Resource
	private TeamMateDao teamMateDao;
	@Resource
	private BaseTeamService baseTeamService;

	@Transactional(readOnly = false)
	public TeamMate update(TeamMate member, UpdateMemberRequest request) {
		member.setDisplayName(request.getDisplayName());
		return teamMateDao.save(member);
	}
	
	/**
	 * 查询工作空间的群主
	 *
	 * @param workspace
	 * @return
	 */
	public TeamMate findOwner(Workspace workspace){
		return findOwner(workspace.getTeam());
	}

	private TeamMate findOwner(BaseTeam team) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("team", Operator.EQ, team));
		filters.add(new SearchFilter("role", Operator.EQ, Roles.OWNER));
		TeamMate owner = teamMateDao.findOne(Specifications.fromFilters(filters, TeamMate.class));
		return owner;
	}

	@Transactional(readOnly = false)
	public TeamMate updateRole(TeamMate member, String role) {
		// 如果是更改为群主，需要改变原先群主的角色
		if (Roles.OWNER.equals(role)){
			TeamMate owner = findOwner(member.getTeam());
			if (owner != null){
				owner.setRole(Roles.ADMIN);
				teamMateDao.save(owner);
			}
			baseTeamService.changeOwner(member.getTeam(), member.getUser().getId());
		}		
		member.setRole(role);
		return teamMateDao.save(member);
	}

	@Transactional(readOnly = false)
	public void delete(TeamMate member) {
		baseTeamService.removeMembers(member.getTeam(), Lists.newArrayList(member));
	}
}
