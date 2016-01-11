package cn.ac.iscas.oncecloudshare.service.extensions.index.model;

import java.io.Serializable;


import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileOwner;

/**
 * @author One
 * 
 *         JMS 索引 消息协议
 * 
 */
public  class IndexEntity<T extends UserRequestEvent>  {

	public IndexEntity(){}

	public enum EntityType implements Serializable {

		CREATE_OR_UPDATE, DELETE;
	}

	public IndexEntity(EntityType entityType, Long fileId, String md5, String fileType, String fileName, String jsonObject, FileOwner fileOwner) {
		super();
		this.entityType = entityType;
		this.fileId = fileId;
		this.md5 = md5;
		this.fileType = fileType;
		this.fileName = fileName;
		this.jsonObject = jsonObject;
		this.fileOwner = fileOwner.getId();
	}

	public IndexEntity(T obj) {
		this.makeEntity(obj);
	}
	
	protected void makeEntity(T object)
	{
		throw new RuntimeException("You must implements in its subclass");
	} 

	protected EntityType entityType;

	protected Long fileId;

	protected String md5;

	protected String fileType;

	protected String fileName;

	protected String jsonObject;

	protected Long fileOwner;
	
	protected Long tenantId;
	

	public String getJsonObject() {
		return jsonObject;
	}

	public Long getFileOwner() {
		return fileOwner;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getFileId() {
		return fileId;
	}

	public String getMd5() {
		return md5;
	}

	public String getFileType() {
		return fileType;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setJsonObject(String jsonObject) {
		this.jsonObject = jsonObject;
	}

	public void setFileOwner(Long fileOwner) {
		this.fileOwner = fileOwner;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}
}
