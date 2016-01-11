package cn.ac.iscas.oncecloudshare.service.extensions.index.model;

import java.io.Serializable;

import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.event.file.FileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

public class UpdateIndexEntity  extends IndexEntity<FileVersionEvent> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1225031843667107575L;

	public UpdateIndexEntity(FileVersionEvent obj ,Long tenantId) {
		super(obj);
		this.tenantId =tenantId;
	}

	@Override
	protected void makeEntity(FileVersionEvent object) {
		
		this.entityType = EntityType.CREATE_OR_UPDATE;

		this.fileId = object.getFileVersion().getFile().getId();

		this.fileName = object.getFileVersion().getFile().getName();

		this.fileOwner = object.getFileVersion().getFile().getOwner().getId();

		this.fileType = object.getFileVersion().getFile().getName().split("\\.")[1];

		this.jsonObject = Gsons.defaultGson().toJson(FileDto.of(object.getFileVersion().getFile()));

		this.md5 = object.getFileVersion().getMd5();
		
	}

}
