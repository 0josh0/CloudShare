package cn.ac.iscas.oncecloudshare.messaging.controller.muc;

import java.util.Set;

import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.messaging.controller.BaseController;
import cn.ac.iscas.oncecloudshare.messaging.controller.PageParam;
import cn.ac.iscas.oncecloudshare.messaging.dto.PageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.muc.MucOccupantDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.muc.MucRoomDto;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucRoom;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.service.authc.AccountService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.MucMessageService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.RoomService;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

@Controller
@RequestMapping (value="/api/muc",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class MucController extends BaseController{
	
	private static final String ROOM_DEFAULT_SORT="createTime";
	private static final String OCCUPANT_DEFAULT_SORT="createTime";

	@Autowired
	RoomService rService;
	
	@Autowired
	OccupantService oService;

	@Autowired
	AccountService accountService;
	
	@Autowired
	MucMessageService mmService;
	
	private MucRoom findRoom(Long roomId){
		MucRoom room=rService.findOne(roomId);
		if(room==null){
			throw new RestException(HttpStatus.NOT_FOUND,"room not exists");
		}
		return room;
	}
	
	private void checkModifiable(MucRoom room){
		if(room.getSpecial()){
			throw new RestException(HttpStatus.FORBIDDEN,"room not modifiable");
		}
	}
	
	private MucOccupant findOccupant(Long roomId,long userId){
		MucOccupant occupant=oService.find(roomId,userId);
		if(occupant==null){
			throw new RestException(HttpStatus.NOT_FOUND,"occupant not exists");
		}
		return occupant;
	}
	
	private void checkOwner(MucRoom room,long userId){
		if(room.getOwner()!=userId){
			throw new RestException(HttpStatus.FORBIDDEN,"permission denied");
		}
	}
	
	private void checkModerator(MucRoom room,long userId){
		if(room.getOwner()!=userId){
			throw new RestException(HttpStatus.FORBIDDEN,"permission denied");
		}
	}
	
	private void checkInRoom(MucRoom room,long userId){
		if(oService.find(room.getId(),userId)==null){
			throw new RestException(HttpStatus.FORBIDDEN,"permission denied");
		}
	}
	
	/**
	 * 当前用户加入的所有room
	 * @return
	 */
	@RequestMapping(value="rooms",method=RequestMethod.GET)
	@ResponseBody
	public String listRooms(PageParam pageParam){
		Long userId=currentUserId();
		pageParam.setSortIfAbsent(ROOM_DEFAULT_SORT);
		Page<MucRoom> page=rService.findByUser(userId,
				pageParam.getPageable(MucRoom.class));
		return Gsons.filterByFields(MucRoomDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,MucRoomDto.TRANSFORMER));
	}

	/**
	 * 创建room
	 * @param subject
	 * @param occupantIds
	 * @param uriBuilder
	 * @return
	 */
	@RequestMapping(value="rooms",method=RequestMethod.POST)
	@ResponseBody
	public String createRoom(@RequestParam String subject,
			@RequestParam Set<Long> occupantIds){
		final Long userId=currentUserId();
		occupantIds=Sets.filter(occupantIds,
				new Predicate<Long>(){
					@Override
					public boolean apply(Long input){
						if(input==null){
							return false;
						}
						TenantUser user=new TenantUser(currentTenantId(),input);
						return 	input.equals(userId)==false &&
								accountService.verifyUserExists(user);
					}
		});
		
		if(occupantIds.size()<2){
			throw new RestException(HttpStatus.BAD_REQUEST,
					"A room must have at least 2 occupants.");
		}
		
		MucRoom room=new MucRoom();
		room.setSubject(Strings.nullToEmpty(subject));
		room.setOwner(userId);
		
		MucOccupant owner=new MucOccupant();
		owner.setUserId(userId);
		owner.setRole(Role.Moderator);
		Set<MucOccupant> occupants=Sets.newHashSet(owner);
		for(Long id:occupantIds){
			MucOccupant occupant=new MucOccupant();
			occupant.setUserId(id);
			occupant.setRole(Role.Participant);
			occupants.add(occupant);
		}
		room=rService.add(room,occupants);
		
		return gson().toJson(MucRoomDto.of(room));
	}
	
	/**
	 * 获取单个room的信息
	 * @param roomId
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}",method=RequestMethod.GET)
	@ResponseBody
	public String getRoom(@PathVariable Long roomId){
		MucRoom room=findRoom(roomId);
		return gson().toJson(MucRoomDto.of(room));
	}
	
	/**
	 * 更新room
	 * @param roomId
	 * @param subject
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}",method=RequestMethod.PUT)
	@ResponseBody
	public String updateRoom(@PathVariable Long roomId,
			@RequestParam String subject){
		MucRoom room=findRoom(roomId);
		checkModifiable(room);
		checkModerator(room,currentUserId());

		room.setSubject(subject);
		rService.upadte(room);
		return ok();
	}
	
	/**
	 * 删除room
	 * @param roomId
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}",method=RequestMethod.DELETE)
	@ResponseBody
	public String deleteRoom(@PathVariable Long roomId){
		MucRoom room=findRoom(roomId);
		checkModifiable(room);
		Long userId=currentUserId();
		checkOwner(room,userId);
		
		rService.safeDelete(roomId,userId);
		return ok();
	}
	
	/**
	 * 获取room成员
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}/occupants",method=RequestMethod.GET)
	@ResponseBody
	public String listOccupants(@PathVariable Long roomId,
			PageParam pageParam){
		MucRoom room=findRoom(roomId);
		checkInRoom(room,currentUserId());
		pageParam.setSortIfAbsent(OCCUPANT_DEFAULT_SORT);
		Page<MucOccupant> page=oService.findByRoomId(roomId,
				pageParam.getPageable(MucOccupant.class));
		return Gsons.filterByFields(MucOccupantDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,MucOccupantDto.TRANSFORMER));
	}
	
	/**
	 * 添加room成员
	 * @param roomId
	 * @param occupantIds
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}/occupants",method=RequestMethod.POST)
	@ResponseBody
	public String addOccupants(@PathVariable Long roomId,
			@RequestParam Set<Long> occupantIds){
		MucRoom room=findRoom(roomId);
		checkModifiable(room);
		checkModerator(room,currentUserId());
		Set<MucOccupant> occupants=Sets.newHashSet();
		for(Long id:occupantIds){
			if(id!=null && accountService.verifyUserExists(
					new TenantUser(currentTenantId(),id))){
				MucOccupant occupant=new MucOccupant();
				occupant.setUserId(id);
				occupant.setRole(Role.Participant);
				occupants.add(occupant);
			}
		}
		try{
			rService.addOccupants(room,occupants);
		}
		catch(DataIntegrityViolationException e){
			throw new RestException(HttpStatus.CONFLICT,
					"occupant(s) already in room");
		}
		return ok();
	}
	
	/**
	 * 批量删除room成员
	 * @param roomId
	 * @param occupantIds
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}/occupants",method=RequestMethod.DELETE)
	@ResponseBody
	public String removeOccupants(@PathVariable Long roomId,
			@RequestParam Set<Long> occupantIds){
		MucRoom room=findRoom(roomId);
		checkModifiable(room);
		checkModerator(room,currentUserId());
		occupantIds.remove(null);
		occupantIds.remove(room.getOwner());
		oService.deleteBatch(roomId,occupantIds);
		return ok();
	}
	
	/**
	 * 删除单个room成员
	 * @return
	 */
	@RequestMapping(value="rooms/{roomId:\\d+}/occupants/{userId:\\d+}",
			method=RequestMethod.DELETE)
	@ResponseBody
	public String removeSingleOccupant(@PathVariable Long roomId,
			@PathVariable Long userId){
		MucOccupant occupant=findOccupant(roomId,userId);
		checkModifiable(occupant.getRoom());
		if(userId.equals(occupant.getRoom().getOwner())){
			throw new RestException(HttpStatus.CONFLICT,
					"cannot delete room owner");
		}
		if(userId.equals(currentUserId())==false){
			checkModerator(occupant.getRoom(),currentUserId());
		}
		oService.delete(occupant);
		return ok();
	}
	
}
