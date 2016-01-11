package cn.ac.iscas.oncecloudshare.messaging.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import cn.ac.iscas.oncecloudshare.messaging.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.messaging.service.chat.ChatMessageService;
import cn.ac.iscas.oncecloudshare.messaging.service.notif.NotifMessageService;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;

@Controller
@RequestMapping (value="/test",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class TestController{
	
	@SuppressWarnings("unused")
	private static Logger logger=LoggerFactory.getLogger(TestController.class);
	
	@Autowired
	NotifMessageService nmService;
	
	@Autowired
	ChatMessageService imService;

	@RequestMapping (value="")
	@ResponseBody
	public Object test(HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		String string=ServletUriComponentsBuilder.newInstance()
				.path("/1/2").path("123").build().toUriString();
		return string;
	}
	
//	@RequestMapping (value="findByConversation")
//	@ResponseBody
//	public String findByConversation() throws Exception{
//		List<ImMessage> list=imService.findByConversation(2,new Conversation(56,57),null);
//		return Gsons.defaultGson().toJson(list);
//	}
	
//	@RequestMapping (value="send",method=RequestMethod.GET)
//	public String send(HttpServletRequest request,
//			Model model) throws Exception{
////		model.addAttribute("userIds",uService.getAllUserIds());
//		return "notif";
//	}
	
//	@RequestMapping (value="doSend",method=RequestMethod.POST)
//	public String doSend(HttpServletRequest request,
//			HttpServletResponse response,
//			@RequestParam(required=true) String msg,
//			@RequestParam long[] to) throws Exception{
////		NotifMessage notif=new NotifMessage(MessageType.NOTIF_SYS,msg);
////		if(to.length==0){
////			NotifSender.getInstance().sendBroadcast(notif);
////		}
////		else{
////			NotifSender.getInstance().sendNotif(notif,Longs.asList(to));
////		}
//		return "redirect:/test/send";
//	}
	
//	@RequestMapping(value="redis",method=RequestMethod.GET)
//	@ResponseBody
//	public String redis(){
////		RedisUtil redisUtil=RedisUtil.getInstance();
////		redisUtil.set("hehe","hehe");
////		System.out.println(redisUtil.get("hehe"));
//		return "";
//	}
	
//	@RequestMapping(value="unreadConv",method=RequestMethod.GET)
//	@ResponseBody
//	public String unreadConv(){
//		return Gsons.defaultGson().toJson(imService.getAllUnreadConv(56));
//	}
	
//	@RequestMapping (value="save")
//	@ResponseBody
//	public String testSave(HttpServletRequest request,
//			@RequestParam(value="content",defaultValue="") String content) throws Exception{
//		
//		NotifMessage notifMessage=new NotifMessage();
//		notifMessage.setType(MessageType.NOTIF_SHARE);
//		notifMessage.setContent(content);
//		notifMessage.setReceiver(1L);
//		
//		nmService.save(notifMessage);
//		
//		System.out.println(notifMessage);
//		
//		return "";
//	}
//	

}
