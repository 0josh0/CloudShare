package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileEvent;

public class WDeleteIndexEntity extends IndexEntity<SpaceFileEvent> {

	public WDeleteIndexEntity(SpaceFileEvent obj, Long tenantId) {
		super(obj);
		this.tenantId = tenantId;
	}

	@Override
	protected void makeEntity(SpaceFileEvent object) {

		this.entityType = EntityType.DELETE;

		this.fileId = object.getFile().getId();

	}

}
