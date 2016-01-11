package cn.ac.iscas.oncecloudshare.service.extensions.msg.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dao.common.BaseTeamDao;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.extensions.msg.service.MessageService;
import cn.ac.iscas.oncecloudshare.service.messaging.model.Room;
import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.service.common.BaseTeamService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/tmp", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class RoomRepairController extends BaseController{
	private static final Logger LOGGER = LoggerFactory.getLogger(RoomRepairController.class);

	@Resource
	private TenantService tenantService;
	@Resource
	private BaseTeamService baseTeamService;
	@Resource
	private MessageService messageService;
	@Resource
	private BaseTeamDao baseTeamDao;

	@RequestMapping(value = "roomRepair")
	@ResponseBody
	public String roomRepair() {
		List<Tenant> tenants = tenantService.findAll(null);
		for (Tenant tenant : tenants) {
			try {
				tenantService.setCurrentTenantManually(tenant.getId());
				List<BaseTeam> teams = baseTeamService.findAll();
				for (BaseTeam team : teams) {
					if (team.getRoomId() == null) {
						Long ownerId = getOwnerId(team);
						if (ownerId != null) {
							try {
								Room room = messageService.createRoom(getSubject(team), ownerId, getOccupantIds(team, ownerId), true);
								team.setRoomId(room.getId());
								baseTeamDao.save(team);
							} catch (Exception e) {
								LOGGER.error(null, e);
							}
						}
					 }
				}
			} catch (Exception e) {
				LOGGER.error(null, e);
			}
		}
		return gson().toJson(ResponseDto.OK);
	}

	private String getSubject(BaseTeam team) {
		if (team instanceof Team) {
			return ((Team) team).getName();
		}
		if (StringUtils.isNotEmpty(team.getDescription())) {
			return team.getDescription().substring(7, team.getDescription().length() - 6);
		}
		return "Team#" + team.getId() + "对应的聊天室";
	}

	private Long getOwnerId(BaseTeam team) {
		if (team instanceof Team) {
			return ((Team) team).getCreateBy().getId();
		}
		List<TeamMate> members = team.getMembers();
		for (TeamMate member : members) {
			if ("owner".equals(member.getRole())) {
				return member.getUser().getId();
			}
		}
		for (TeamMate member : members) {
			if (UserStatus.ACTIVE.equals(member.getUser().getStatus())) {
				return member.getUser().getId();
			}
		}
		return null;
	}

	private List<Long> getOccupantIds(BaseTeam team, Long ownerId) {
		List<Long> occupantIds = Lists.newArrayList();
		for (TeamMate member : team.getMembers()) {
			if (!ownerId.equals(member.getUser().getId()) && UserStatus.ACTIVE.equals(member.getUser().getStatus())) {
				occupantIds.add(member.getUser().getId());
			}
		}
		return occupantIds;
	}
}
