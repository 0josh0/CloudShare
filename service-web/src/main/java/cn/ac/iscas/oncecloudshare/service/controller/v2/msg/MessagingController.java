package cn.ac.iscas.oncecloudshare.service.controller.v2.msg;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.PrincipalService;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping (value="/api/v2/messaging",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class MessagingController extends BaseController {
	
	private static final String SECRET_KEY_PARAM="x-msg-secret-key";
	
	@Autowired
	PrincipalService pService;
	
	@Autowired
	UserService uService;
	
	private void authenticate(HttpServletRequest req){
		
		if(!Constants.getMsgSecretKey().equals(
				req.getHeader(SECRET_KEY_PARAM))){
			throw new RestException(ErrorCode.UNAUTHORIZED);
		}
	}
	
	private String serialize(User user){
		if(user==null){
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		return gson().toJson(UserDto.forMessaging(user));
	}
	
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	@ResponseBody
	public String getUserInfo(HttpServletRequest req,@RequestParam Long userId){
		authenticate(req);
		User user=uService.find(userId);
		return serialize(user);
	}
	
	@RequestMapping(value = "/ticketInfo", method = RequestMethod.GET)
	@ResponseBody
	public String getTicketInfo(HttpServletRequest req,@RequestParam String ticket){
		authenticate(req);
		User user=null;
		Object principal=pService.getPrincipal(ticket);
		if(principal instanceof UserPrincipal){
			UserPrincipal userPrincipal=(UserPrincipal)principal;
			user=uService.find(userPrincipal.getUserId());
		}

		return serialize(user);
	}
	
	@RequestMapping(value = "/sendNotif", method = RequestMethod.GET)
	@ResponseBody
	public String sendNotif(){
		String content="hehe "+new Date().toLocaleString();
		NotificationType type=new NotificationType(){

			@Override
			public String getType(){
				return "test";
			}
		};
		
		runtimeContext.getNotifService()
			.sendNotif(new Notification(type,content, null, 1L));
		return ok();
		
	}
}
