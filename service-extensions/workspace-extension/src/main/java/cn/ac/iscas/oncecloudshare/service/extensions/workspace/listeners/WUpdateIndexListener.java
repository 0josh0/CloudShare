package cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.event.file.FileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WUpdateIndexEntity;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;

@Component
public class WUpdateIndexListener extends WIndexListener<SpaceFileVersionEvent> {

	@Override
	@SubscribeEvent
	public void handleEvent(SpaceFileVersionEvent event) {

		if (event.getEventType() != FileVersionEvent.EVENT_UPLOAD)
			return;

		WUpdateIndexEntity entity = new WUpdateIndexEntity(event, tenantService.getCurrentTenant().getId());

		this.notifyService.sendNotif(entity);
	}

}
