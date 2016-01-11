package cn.ac.iscas.oncecloudshare.messaging.controller.notif;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import cn.ac.iscas.oncecloudshare.messaging.controller.BaseController;
import cn.ac.iscas.oncecloudshare.messaging.controller.PageParam;
import cn.ac.iscas.oncecloudshare.messaging.dto.PageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.notif.NotifMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;
import cn.ac.iscas.oncecloudshare.messaging.service.notif.NotifMessageService;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.SearchFilter.Operator;

import com.google.common.primitives.Longs;

@Controller
@RequestMapping (value="/api/notifs",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class NotifController extends BaseController {

	private static final String DEFAULT_SORT="-ts";
	
	@Autowired
	NotifMessageService nmService;
	
	@RequestMapping(method=RequestMethod.GET)
	@ResponseBody
	public String list(PageParam pageParam){
		Long userId=currentUserId();
		pageParam.setSortIfAbsent(DEFAULT_SORT);
		Page<NotifMessage> page=nmService.findByReceiver(userId,
				pageParam.getPageable(NotifMessage.class));
		return Gsons.filterByFields(NotifMessageDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,NotifMessageDto.TRANSFORMER));
	}
	
	@RequestMapping(value="search",method=RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q,PageParam pageParam){
		Long userId=currentUserId();
		pageParam.setSortIfAbsent(DEFAULT_SORT);
		List<SearchFilter> searchFilters=SearchFilter.parseQuery(q);
		searchFilters.add(new SearchFilter("receiver",Operator.EQ,userId));
		Page<NotifMessage> page=nmService.search(searchFilters,
				pageParam.getPageable(NotifMessage.class));
		return Gsons.filterByFields(NotifMessageDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,NotifMessageDto.TRANSFORMER));
	}
	
	@RequestMapping(method=RequestMethod.PUT,params="readFlag=true")
	@ResponseBody
	public String markAsReadBatch(
			@RequestParam(value="ids",required=true) long[] ids){
		Long userId=currentUserId();
		nmService.markAsReadBatch(userId,Longs.asList(ids));
		return ok();
	}

	@RequestMapping(method=RequestMethod.DELETE)
	@ResponseBody
	public String deleteBatch(
			@RequestParam(value="ids",required=true) long[] ids){
		Long userId=currentUserId();
		nmService.deleteBatch(userId,Longs.asList(ids));
		return ok();
	}
}
