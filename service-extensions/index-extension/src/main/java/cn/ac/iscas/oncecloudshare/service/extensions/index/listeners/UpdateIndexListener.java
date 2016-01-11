package cn.ac.iscas.oncecloudshare.service.extensions.index.listeners;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.event.file.FileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.index.model.UpdateIndexEntity;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;

@Component
public class UpdateIndexListener extends IndexListener<FileVersionEvent>  {

	@Override
	@SubscribeEvent
	public void handleEvent(FileVersionEvent event) {

		if (event.getEventType() != FileVersionEvent.EVENT_UPLOAD)
			return;

		UpdateIndexEntity entity = new UpdateIndexEntity(event ,tenantService.getCurrentTenant().getId());
		
		this.notifyService.sendNotif(entity);
	}

}
