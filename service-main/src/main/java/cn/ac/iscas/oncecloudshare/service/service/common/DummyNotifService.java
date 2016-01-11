package cn.ac.iscas.oncecloudshare.service.service.common;

import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;

/**
 * A NotifService that does nothing.
 * 
 * @author One
 * 
 * @deprecated no need in saas
 */

@Deprecated
//@Service
public class DummyNotifService implements NotifService {

	@Override
	public void sendNotif(Notification notification){
		
	}

}
