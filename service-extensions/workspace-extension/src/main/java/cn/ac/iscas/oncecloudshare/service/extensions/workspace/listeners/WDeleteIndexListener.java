package cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WDeleteIndexEntity;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;

@Component
public class WDeleteIndexListener extends WIndexListener<SpaceFileEvent>   {


	@Override
	@SubscribeEvent
	public void handleEvent(SpaceFileEvent event) {

		if (event.getEventType() != FileEvent.EVENT_TRASH)
			return;

		WDeleteIndexEntity entity = new WDeleteIndexEntity(event ,tenantService.getCurrentTenant().getId());
		
		this.notifyService.sendNotif(entity);
	}

}
