package cn.ac.iscas.oncecloudshare.service.extensions.device.actions;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.action.log.ActionLoggerManager;
import cn.ac.iscas.oncecloudshare.service.action.log.annotations.ActionLogger;
import cn.ac.iscas.oncecloudshare.service.extensions.device.events.AdminDeviceEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceUser;
import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;
import cn.ac.iscas.oncecloudshare.service.model.log.ActionType;
import cn.ac.iscas.oncecloudshare.service.model.log.TargetType;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.common.ActionLogService;

@Component
public class AdminDeviceLoggers {
	@Resource
	private UserService userService;
	@Resource
	private ActionLoggerManager actionLoggerManager;
	@Resource
	private ActionLogService actionLogService;
	
	@PostConstruct
	public void init(){
		actionLoggerManager.register(this);
		for (TargetType targetType : DeviceTargetType.values()){
			actionLogService.register(targetType);
		}
		for (@SuppressWarnings("unused") ActionType actionType : AdminDeviceAction.values()){
			
		}
	}

	@ActionLogger
	public boolean logAdminDeviceEvent(AdminDeviceEvent event, ActionLog log) {
		log.setUser(userService.find(event.getPrincipal().getUserId()));
		log.setTargetType(DeviceTargetType.EXT_DEVICE.getCode());
		DeviceUser deviceUser = event.getDeviceUser();
		if (deviceUser != null) {
			log.setTargetId(event.getDeviceUser().getId().toString());
		}
		String deviceInfo = new StringBuilder().append(deviceUser.getUser().getName()).append("的设备").append(deviceUser.getDevice().getType())
				.append(":").append(deviceUser.getDevice().getMac()).toString();
		switch (event.getEventType()) {
		case AdminDeviceEvent.EVENT_AGREED:
			log.setType(AdminDeviceAction.AGREE.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "通过了" + deviceInfo);
			break;
		case AdminDeviceEvent.EVENT_DISAGREED:
			log.setType(AdminDeviceAction.DISAGREE.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "拒绝了" + deviceInfo);
			break;
		case AdminDeviceEvent.EVENT_ENABLED:
			log.setType(AdminDeviceAction.ENABLE.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "启用了" + deviceInfo);
			break;
		case AdminDeviceEvent.EVENT_DISABLED:
			log.setType(AdminDeviceAction.DISABLE.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "禁用了" + deviceInfo);
			break;
		}
		return true;
	}
}