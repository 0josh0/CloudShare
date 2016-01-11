package cn.ac.iscas.oncecloudshare.service.extensions.index.model;


import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent;

public class DeleteIndexEntity extends IndexEntity<FileEvent>   {


	public DeleteIndexEntity(FileEvent obj ,Long tenantId) {
		super(obj);
		this.tenantId =tenantId;
	}

	@Override
	protected void makeEntity(FileEvent object) {
		
		this.entityType = EntityType.DELETE;

		this.fileId = object.getFile().getId();

	}

}
