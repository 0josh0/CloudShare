package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

public class WUpdateIndexEntity  extends IndexEntity<SpaceFileVersionEvent>  {


	public WUpdateIndexEntity(SpaceFileVersionEvent obj ,Long tenantId) {
		super(obj);
		this.tenantId =tenantId;
	}

	@Override
	protected void makeEntity(SpaceFileVersionEvent object) {
		
		this.entityType = EntityType.CREATE_OR_UPDATE;

		this.fileId = object.getFileVersion().getFile().getId();

		this.fileName = object.getFileVersion().getFile().getName();

		this.fileOwner = object.getWorkspace().getSpace().getId();

		this.fileType = object.getFileVersion().getFile().getName().split("\\.")[1];

		this.jsonObject = Gsons.defaultGson().toJson(SpaceFileDto.defaultTransformer.apply((object.getFileVersion().getFile())));

		this.md5 = object.getFileVersion().getMd5();
		
	}

}
