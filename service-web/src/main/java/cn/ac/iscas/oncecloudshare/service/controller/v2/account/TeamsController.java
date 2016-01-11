package cn.ac.iscas.oncecloudshare.service.controller.v2.account;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.TeamDto;
import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.service.account.TeamService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

@Controller
@RequestMapping(value = "/api/v2/teams", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class TeamsController extends BaseController {
	@Resource
	private TeamService teamService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String create(@Valid TeamDto.CreateRequest request) {
		Team team = teamService.create(currentUser(), request);
		return gson().toJson(TeamDto.toBrief.apply(team));
	}

	/**
	 * 查询我加入的讨论组
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String findJoined(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("members.user", Operator.EQ, currentUser()));
		Page<Team> teams = teamService.findAll(filters, pageParam.getPageable(Team.class));
		return Gsons.filterByFields(TeamDto.class, pageParam.getFields()).toJson(PageDto.of(teams, TeamDto.toBrief));
	}
}
