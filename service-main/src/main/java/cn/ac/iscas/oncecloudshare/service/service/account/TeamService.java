package cn.ac.iscas.oncecloudshare.service.service.account;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.TeamDao;
import cn.ac.iscas.oncecloudshare.service.dao.authorization.TeamMateDao;
import cn.ac.iscas.oncecloudshare.service.dto.account.TeamDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.TeamDto.InviteRequest;
import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam.Status;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.common.BaseTeamService;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class TeamService {
	@Resource
	private TeamDao teamDao;
	@Resource
	private TeamMateDao teamMateDao;
	@Resource
	private UserService userService;
	@Resource
	private BaseTeamService baseTeamService;

	public Team findOne(long id) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("id", Operator.EQ, id));
		return teamDao.findOne(Specifications.fromFilters(filters, Team.class));
	}

	public Team findOneByName(String name) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("name", Operator.EQ, name));
		return teamDao.findOne(Specifications.fromFilters(filters, Team.class));
	}

	public Page<Team> findAll(List<SearchFilter> filters, Pageable pageable) {
		return teamDao.findAll(Specifications.fromFilters(filters, Team.class), pageable);
	}

	public Page<Team> findJoined(long userId, Status status, Pageable pageable) {
		return teamDao.findJoined(userId, status, pageable);
	}

	@Transactional(readOnly = false)
	public Team create(User creator, TeamDto.CreateRequest request) {
		// if (findOneByName(request.getName()) != null) {
		// throw new DuplicateTeamException();
		// }
		// 创建小组
		final Team team = new Team();
		team.setDescription(request.getDescription());
		team.setMembersCount(1);
		team.setName(request.getName());
		team.setCreateBy(creator);

		List<User> members = null;
		if (ArrayUtils.isNotEmpty(request.getMembers())) {
			List<SearchFilter> filters = Lists.newArrayList(new SearchFilter("id", Operator.IN, request.getMembers()));
			filters.add(new SearchFilter("status", Operator.EQ, UserStatus.ACTIVE));
			members = userService.findAll(filters);
			members.remove(creator);
		}

		baseTeamService.createTeam(team, team.getName(), team.getCreateBy(), members, new Function<User, TeamMate>() {
			@Override
			public TeamMate apply(User input) {
				return new TeamMate(team, input);
			}
		});

		return team;
	}

	@Transactional(readOnly = false)
	public Team update(Team team, TeamDto.CreateRequest request) {
		team.setName(request.getName());
		team.setDescription(request.getDescription());
		return teamDao.save(team);
	}

	public TeamMate findTeamMate(Team team, long userId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("team", Operator.EQ, team));
		filters.add(new SearchFilter("user.id", Operator.EQ, userId));
		return teamMateDao.findOne(Specifications.fromFilters(filters, TeamMate.class));
	}

	/**
	 * 邀请成员
	 * 
	 * @param workspace
	 * @param inviteRequest
	 * @return
	 */
	@Transactional(readOnly = false)
	public List<TeamMate> invite(Team team, InviteRequest inviteRequest) {
		List<TeamMate> toAdd = Lists.newArrayList();
		for (long userId : inviteRequest.getTargets()) {
			if (team.hasMember(userId)){
				continue;
			}
			User user = userService.find(userId);
			if (user == null) {
				continue;
			}
			TeamMate teamMate = new TeamMate();
			teamMate.setTeam(team);
			teamMate.setUser(user);
			teamMate.setJoinTime(new Date());
			toAdd.add(teamMate);
		}
		if (toAdd.size() > 0){
			return baseTeamService.addMembers(team, toAdd);
		}
		return Lists.newArrayList();
	}

	@Transactional(readOnly = false)
	public void deleteMate(TeamMate mate) {
		baseTeamService.removeMembers(mate.getTeam(), Lists.<TeamMate>newArrayList(mate));
	}
}
