package cn.ac.iscas.oncecloudshare.service.extensions.index.listeners;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.index.model.DeleteIndexEntity;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;

@Component
public class DeleteIndexListener extends IndexListener<FileEvent>   {


	@Override
	@SubscribeEvent
	public void handleEvent(FileEvent event) {

		if (event.getEventType() != FileEvent.EVENT_TRASH)
			return;

		DeleteIndexEntity entity = new DeleteIndexEntity(event ,tenantService.getCurrentTenant().getId());
		
		this.notifyService.sendNotif(entity);
	}

}
