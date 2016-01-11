package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.TeamMateDao;
import cn.ac.iscas.oncecloudshare.service.dao.common.BaseTeamDao;
import cn.ac.iscas.oncecloudshare.service.messaging.model.Room;
import cn.ac.iscas.oncecloudshare.service.messaging.service.MessageService;
import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.guava.Functions;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = false)
public class BaseTeamService {
	@Resource
	private BaseTeamDao baseTeamDao;
	@Resource
	private TeamMateDao teamMateDao;
	@Resource
	private UserService userService;
	@Resource
	private RuntimeContext runtimeContext;
	
	public <T extends BaseTeam> T createTeam(T team, String subject, User owner){
		MessageService messageService = runtimeContext.getMessageService();
		Room room = messageService.createRoom(subject, owner.getId(), Collections.<Long> emptyList(), true);
		try{
			team.setRoomId(room.getId());
			baseTeamDao.save(team);
			// 添加群主
			TeamMate teamMate = new TeamMate();
			teamMate.setJoinTime(new Date());
			teamMate.setRole("owner");
			teamMate.setUser(owner);
			teamMate.setTeam(team);
			teamMateDao.save(teamMate);
		} catch(Exception e){
			// 如果操作失败，删除对应的room
			if (room != null){
				messageService.deleteRoom(room.getId());
			}
			throw e;
		}
		return team;
	}

	public Team createTeam(Team team, String subject, User owner, List<User> members, Function<User, TeamMate> memberTransfer) {
		int membersCount = members == null ? 0 : members.size();
		List<Long> occupantIds;
		if (membersCount == 0) {
			occupantIds = Collections.<Long> emptyList();
		} else {
			occupantIds = Lists.transform(members, Functions.IDENTITY_TO_ID);
		}
		MessageService messageService = runtimeContext.getMessageService();
		Room room = messageService.createRoom(subject, owner.getId(), occupantIds, true);
		try {
			team.setRoomId(room.getId());
			team.setMembersCount(membersCount + 1);			
			baseTeamDao.save(team);
			// 添加群主
			teamMateDao.save(memberTransfer.apply(owner));
			// 添加成员
			if (membersCount > 0) {
				for (User user : members) {
					teamMateDao.save(memberTransfer.apply(user));
				}
			}
		} catch (Exception e) {
			// 如果操作失败，删除对应的room
			if (room != null) {
				messageService.deleteRoom(room.getId());
			}
			throw e;
		}
		return team;
	}

	public List<TeamMate> addMembers(BaseTeam team, List<TeamMate> members) {
		List<Long> addedUsers = Lists.newArrayList();
		List<TeamMate> addedMembers = Lists.newArrayList();
		for (TeamMate member : members) {
			if (member.getId() == null && !team.hasMember(member.getUser().getId()) && addedUsers.indexOf(member.getUser().getId()) == -1) {
				member.setTeam(team);
				member = teamMateDao.save(member);
				addedMembers.add(member);
				addedUsers.add(member.getUser().getId());
			}
		}
		if (addedUsers.size() > 0) {
			baseTeamDao.updateMemebersCount(team.getId(), addedUsers.size());
			if (team.getRoomId() != null) {
				runtimeContext.getMessageService().addOccupants(team.getRoomId(), addedUsers);
			}
		}
		return addedMembers;
	}

	@Transactional(readOnly = false)
	public void removeMembers(BaseTeam team, List<TeamMate> members) {
		List<Long> removedUsers = Lists.newArrayList();
		for (TeamMate member : members) {
			if (team.hasMember(member.getUser().getId()) && removedUsers.indexOf(member.getUser().getId()) == -1) {
				teamMateDao.delete(member);
				removedUsers.add(member.getUser().getId());
			}
		}
		if (removedUsers.size() > 0) {
			baseTeamDao.updateMemebersCount(team.getId(), -removedUsers.size());
			// TODO: 当最后一个用户退出小组
			// 处理room
			if (team.getRoomId() != null) {
				runtimeContext.getMessageService().removeOccupants(team.getRoomId(), removedUsers);
			}
		}
	}

	public void changeOwner(BaseTeam team, Long ownerId) {
		if (team.getRoomId() != null){
			runtimeContext.getMessageService().changeOwner(team.getRoomId(), ownerId);
		}
	}
	
	@Transactional(readOnly = true)
	public List<BaseTeam> findAll(){
		return Lists.newArrayList(baseTeamDao.findAll());
	}
}
