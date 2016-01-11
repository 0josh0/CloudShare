package cn.ac.iscas.oncecloudshare.messaging.service;

import cn.ac.iscas.oncecloudshare.messaging.service.authc.AccountService;
import cn.ac.iscas.oncecloudshare.messaging.service.chat.ChatMessageService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.MucMessageService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.RoomService;
import cn.ac.iscas.oncecloudshare.messaging.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.messaging.utils.SpringUtil;


public class Services {

	public static AccountService getAccountService(){
		return SpringUtil.getBean(AccountService.class);
	}
	
	public static ChatMessageService getChatMessageService(){
		return SpringUtil.getBean(ChatMessageService.class);
	}
	
	public static MucMessageService getMucMessageService(){
		return SpringUtil.getBean(MucMessageService.class);
	}
	
	public static RoomService getRoomService(){
		return SpringUtil.getBean(RoomService.class);
	}
	
	public static OccupantService getOccupantService(){
		return SpringUtil.getBean(OccupantService.class);
	}
	
	public static TenantService getTenantService(){
		return SpringUtil.getBean(TenantService.class);
	}
}
