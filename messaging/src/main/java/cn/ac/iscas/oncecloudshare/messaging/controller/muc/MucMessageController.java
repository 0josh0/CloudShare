package cn.ac.iscas.oncecloudshare.messaging.controller.muc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;

import cn.ac.iscas.oncecloudshare.messaging.controller.BaseController;
import cn.ac.iscas.oncecloudshare.messaging.controller.PageParam;
import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.PageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.muc.MucMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.muc.UnreadMucMessageDigest;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;
import cn.ac.iscas.oncecloudshare.messaging.service.authc.AccountService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.MucMessageService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.RoomService;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;


@Controller
@RequestMapping (value="/api/muc",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class MucMessageController extends BaseController {
	
	private static final String MESSAGE_DEFAULT_SORT="-ts";
	private static final String OCCUPANT_DEFAULT_SORT="createTime";

	@Autowired
	RoomService rService;
	
	@Autowired
	OccupantService oService;

	@Autowired
	AccountService accountService;
	
	@Autowired
	MucMessageService mmService;
	
	@RequestMapping(value="rooms/{roomId:\\d+}/messages",method=RequestMethod.GET)
	@ResponseBody
	public String listMucMessages(@PathVariable Long roomId,
			PageParam pageParam){
		Long userId=currentUserId();
		MucOccupant occupant=oService.find(roomId,userId);
		if(occupant==null){
			throw new RestException(HttpStatus.FORBIDDEN,
					"permission denied");
		}
		pageParam.setSortIfAbsent(MESSAGE_DEFAULT_SORT);
		Page<MucMessage> page=mmService.findByRoomIdAndTsGreaterThan(
				roomId,occupant.getCreateTime().getTime(),
				pageParam.getPageable(MucMessage.class));
		return Gsons.filterByFields(MucMessage.class,pageParam.getFields())
				.toJson(PageDto.of(page,MucMessageDto.TRANSFORMER));
	}
	
	@RequestMapping(value="rooms/{roomId:\\d+}/messages/search",method=RequestMethod.GET)
	@ResponseBody
	public String searchMucMessages(@PathVariable Long roomId,
			@RequestParam String keyword,
			@RequestParam Long begin,
			@RequestParam(required=false) Long end,
			PageParam pageParam){
		if(Strings.isNullOrEmpty(keyword)){
			throw new RestException(BasicErrorCode.BAD_REQUEST);
		}
		if(end==null){
			end=System.currentTimeMillis();
		}
		
		Long userId=currentUserId();
		MucOccupant occupant=oService.find(roomId,userId);
		if(occupant==null){
			throw new RestException(HttpStatus.FORBIDDEN,
					"permission denied");
		}
		
		pageParam.setSortIfAbsent(MESSAGE_DEFAULT_SORT);
		Page<MucMessage> page=mmService.searchMessagesInRoom(keyword,
				roomId,begin,end,pageParam.getPageable(MucMessage.class));
		return Gsons.filterByFields(MucMessage.class,pageParam.getFields())
				.toJson(PageDto.of(page,MucMessageDto.TRANSFORMER));
	}
	
	@RequestMapping(value="/messages/unreadDigest",method=RequestMethod.GET)
	@ResponseBody
	public String listUnreadMucMessage(PageParam pageParam){
		Long userId=currentUserId();
		pageParam.setSortIfAbsent(OCCUPANT_DEFAULT_SORT);
		Page<UnreadMucMessageDigest> page=mmService.findUnreadMucMessages(userId,
				pageParam.getPageable(MucOccupant.class));
		return Gsons.filterByFields(UnreadMucMessageDigest.class,pageParam.getFields())
				.toJson(PageDto.of(page));
	}
	
	@RequestMapping(value="rooms/{roomId:\\d+}/messages",method=RequestMethod.PUT)
	@ResponseBody
	public String updateReadSeq(@PathVariable Long roomId,
			@RequestParam Long readSeq){
		Long userId=currentUserId();
		oService.updateReadSeq(roomId,userId,readSeq);
		return ok();
	}
}
