package cn.ac.iscas.oncecloudshare.messaging.controller.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.messaging.controller.BaseController;
import cn.ac.iscas.oncecloudshare.messaging.controller.PageParam;
import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.PageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.chat.ChatMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.chat.ConversationDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.chat.UnreadConversationDigest;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.ChatMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.Conversation;
import cn.ac.iscas.oncecloudshare.messaging.service.chat.ChatMessageService;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;

@Controller
@RequestMapping (value="/api/chat",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class ChatController extends BaseController{

	private static final String DEFAULT_SORT="-ts";

	private static final String CONV_DEFAULT_SORT="-updateTime";
	
	@Autowired
	private ChatMessageService cmService;
	
	@RequestMapping(value="convs",method=RequestMethod.GET)
	@ResponseBody
	public String listConversions(PageParam pageParam){
		Long userId=currentUserId();
		pageParam.setSortIfAbsent(CONV_DEFAULT_SORT);
		Page<Conversation> page=cmService.findConversationsByUserId(userId,
				pageParam.getPageable(Conversation.class));
		return Gsons.filterByFields(ConversationDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,ConversationDto.TRANSFORMER));
	}
	
	@RequestMapping(value="convs/{oppositeId:\\d+}",method=RequestMethod.GET)
	@ResponseBody
	public String getConversation(@PathVariable Long oppositeId){
		Conversation conv=cmService.findConversation(currentUserId(),oppositeId);
		if(conv==null){
			throw new RestException(BasicErrorCode.NOT_FOUND,
					"conversation not exists.");
		}
		return gson().toJson(ConversationDto.of(conv));
	}
	
	@RequestMapping(value="convs/{oppositeId:\\d+}/messages",method=RequestMethod.GET)
	@ResponseBody
	public String listMessagesByConversion(@PathVariable Long oppositeId,PageParam pageParam){
		Long userId=currentUserId();
		pageParam.setSortIfAbsent(DEFAULT_SORT);
		Page<ChatMessage> page=cmService.findByConversation(userId,oppositeId,
				pageParam.getPageable(ChatMessage.class));
		return Gsons.filterByFields(ChatMessageDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,ChatMessageDto.TRANSFORMER));
	}
	
	@RequestMapping(value="convs/unreadDigest",method=RequestMethod.GET)
	@ResponseBody
	public String listUnreadConversationDigest(PageParam pageParam){
		Long userId=currentUserId();
		Page<UnreadConversationDigest> page=cmService.getAllUnreadConv(userId,
				pageParam.getPageable(Conversation.class));
		return Gsons.filterByFields(UnreadConversationDigest.class,pageParam.getFields())
				.toJson(PageDto.of(page));
	}
	
	@RequestMapping(value="convs/{oppositeId:\\d+}",params="readSeq",method=RequestMethod.PUT)
	@ResponseBody
	public String updateReadSeq(@PathVariable Long oppositeId,
			@RequestParam long readSeq){
		Long user=currentUserId();
		if(cmService.updateReadSeq(user,oppositeId,readSeq)){
			return gson().toJson(ResponseDto.OK);
		}
		else{
			throw new RestException(BasicErrorCode.BAD_REQUEST,
					"invalid paramter, see /api/chat/convs/{oppositeId} for details.");
		}
	}
	
	@RequestMapping(value="convs/{oppositeId:\\d+}/messages/search",method=RequestMethod.GET)
	@ResponseBody
	public String searchMessages(@PathVariable Long oppositeId,
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
		pageParam.setSortIfAbsent(DEFAULT_SORT);
		Page<ChatMessage> page=cmService.searchByConversation(keyword,begin,end,
				currentUserId(),oppositeId,pageParam.getPageable(ChatMessage.class));
		return Gsons.filterByFields(ChatMessageDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,ChatMessageDto.TRANSFORMER));
	}
	
	@RequestMapping(value="messages",method=RequestMethod.DELETE)
	@ResponseBody
	public String deleteMessagesBatch(
			@RequestParam(value="ids",required=true) long[] ids){
		Long user=currentUserId();
		cmService.deleteBatch(user,Longs.asList(ids));
		return gson().toJson(ResponseDto.OK);
	}
}
