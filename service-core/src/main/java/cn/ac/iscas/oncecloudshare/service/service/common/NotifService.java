package cn.ac.iscas.oncecloudshare.service.service.common;

import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;


public interface NotifService extends ServiceProvider{

	void sendNotif(Notification notification);
	
}
