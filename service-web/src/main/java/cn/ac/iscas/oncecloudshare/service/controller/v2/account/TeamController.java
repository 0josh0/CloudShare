package cn.ac.iscas.oncecloudshare.service.controller.v2.account;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.NotifTypes;
import cn.ac.iscas.oncecloudshare.service.dto.account.TeamDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.TeamMateDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam.Status;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.account.TeamService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Controller
@RequestMapping(value = "/api/v2/teams/{id:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class TeamController extends BaseController {
	@Resource
	private TeamService teamService;

	@InitBinder({ "team", "master" })
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("abcdefg");
	}

	@ModelAttribute
	public void initModel(Model model, @PathVariable("id") long teamId) {
		Team team = teamService.findOne(teamId);
		if (team == null || Status.DELETED.equals(team.getStatus())) {
			throw new RestException(ErrorCode.NOT_FOUND, "team_not_found");
		}
		model.addAttribute("team", team);
		TeamMate master = teamService.findTeamMate(team, currentUserId());
		if (master == null) {
			throw new RestException(ErrorCode.FORBIDDEN, "access_unjoined_team");
		}
		model.addAttribute("master", master);
	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	public String update(@ModelAttribute("team") Team team, @ModelAttribute("master") TeamMate master, TeamDto.CreateRequest request) {
		team = teamService.update(team, request);
		return gson().toJson(TeamDto.toBrief.apply(team));
	}

	@RequestMapping(value = "invite", method = RequestMethod.POST)
	@ResponseBody
	public String invite(@ModelAttribute("team") Team team, @ModelAttribute("master") TeamMate master, TeamDto.InviteRequest request) {
		List<TeamMate> members = teamService.invite(team, request);
		// 发送通知
		StringBuilder content = new StringBuilder();
		int idx = 0;
		for (; idx < members.size(); idx++) {
			User user = members.get(idx).getUser();
			content.append(user.getName()).append(",");
			if (idx > 4) {
				break;
			}
		}
		content.setLength(content.length() - 1);
		if (idx < members.size() - 1) {
			content.append("等").append(members.size()).append("名用户");
		}
		content.append("加入了").append(team.getName());
		Set<Long> to = Sets.newHashSet();
		to.addAll(Collections2.filter(Lists.transform(team.getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
		to.addAll(Collections2.filter(Lists.transform(members, TeamMate.TO_USERID), Predicates.notNull()));
		sendNotif(NotifTypes.Team.JOINED, content.toString(), getNotifAttrs(team, master, members), ImmutableList.<Long> copyOf(to));

		return gson().toJson(Lists.transform(members, TeamMateDto.DEFAULT_TRANSFORMER));
	}

	@RequestMapping(value = "members", method = RequestMethod.GET)
	@ResponseBody
	public String members(@ModelAttribute("team") Team team) {
		List<TeamMate> members = team.getMembers();
		return gson().toJson(Lists.transform(members, TeamMateDto.DEFAULT_TRANSFORMER));
	}

	/**
	 * 移除用户
	 * 
	 * @param team
	 * @param master
	 * @param memberId
	 * @return
	 */
	@RequestMapping(value = "members/{memberId:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String kick(@ModelAttribute("team") Team team, @ModelAttribute("master") TeamMate master, @PathVariable long memberId) {
		// 只有创建者有权限移除用户
		if (!currentUser().equals(team.getCreateBy())) {
			throw new RestException(ErrorCode.FORBIDDEN, "master_not_team_creator");
		}
		TeamMate slave = teamService.findTeamMate(team, memberId);
		if (slave == null) {
			throw new RestException(ErrorCode.NOT_FOUND, "target_teammate_not_found");
		}
		// QQ好像不能移除自己
		if (master.equals(slave)) {
			throw new RestException(ErrorCode.FORBIDDEN, "cannot_remove_team_creator");
		}
		teamService.deleteMate(slave);

		// 发送通知
		StringBuilder content = new StringBuilder().append(slave.getUser().getName()).append("退出了").append(team.getName());
		Set<Long> to = Sets.newHashSet();
		to.addAll(Collections2.filter(Lists.transform(team.getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
		to.add(slave.getUser().getId());
		sendNotif(NotifTypes.Team.KICKED, content.toString(), getNotifAttrs(team, master, slave), ImmutableList.<Long> copyOf(to));

		return ok();
	}

	/**
	 * 退出讨论组
	 * 
	 * @param team
	 * @param master
	 * @param memberId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public String exit(@ModelAttribute("team") Team team, @ModelAttribute("master") TeamMate master) {
		teamService.deleteMate(master);

		Set<Long> to = Sets.newHashSet();
		to.addAll(Collections2.filter(Lists.transform(team.getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
		to.add(master.getUser().getId());
		sendNotif(NotifTypes.Team.KICKED, master.getUser().getName() + "退出了" + team.getName(), getNotifAttrs(team, master, master),  ImmutableList.<Long>copyOf(to));

		return ok();
	}

	public Map<String, Object> getNotifAttrs(Team team, TeamMate master, TeamMate slave) {
		return getNotifAttrs(team, master, Lists.newArrayList(slave));
	}

	public Map<String, Object> getNotifAttrs(Team team, TeamMate master, Collection<TeamMate> slaves) {
		Map<String, Object> attrs = Maps.newHashMap();
		// workspace
		TeamDto teamDto = TeamDto.toBrief.apply(team);
		attrs.put("team", teamDto);
		// master
		UserDto masterDto = new UserDto();
		masterDto.id = master.getUser().getId();
		masterDto.name = master.getUser().getName();
		attrs.put("master", masterDto);
		// slaves
		if (slaves != null && slaves.size() > 0) {
			List<UserDto> slaveDtos = Lists.newArrayList();
			for (TeamMate slave : slaves) {
				UserDto slaveDto = new UserDto();
				slaveDto.id = slave.getUser().getId();
				slaveDto.name = slave.getUser().getName();
				slaveDtos.add(slaveDto);
			}
			attrs.put("slaves", slaveDtos);
		}

		return attrs;
	}
}