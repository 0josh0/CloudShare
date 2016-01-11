package cn.ac.iscas.oncecloudshare.service.service.authorization.principal;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFile;

public final class UploadPrincipal implements Principal {

	private static final long serialVersionUID=1L;

	/**
	 * 如果不为null，表示可以向该文件夹上传新文件
	 */
	public Long parentId;

	/**
	 * 如果不为null，表示可以上传该文件的新版本
	 */
	public Long fileId;
	
	/**
	 * 上传者的id
	 */
	public Long uploaderId;
	
	public UploadPrincipal(){
	}

	public Long getParentId(){
		return parentId;
	}

	public void setParentId(Long parentId){
		this.parentId=parentId;
	}

	public Long getFileId(){
		return fileId;
	}

	public void setFileId(Long fileId){
		this.fileId=fileId;
	}

	public Long getUploaderId() {
		return uploaderId;
	}

	public void setUploaderId(Long uploaderId) {
		this.uploaderId = uploaderId;
	}

	public static UploadPrincipal createForFileUpload(GenericFile<?> parentFolder){
		UploadPrincipal up=new UploadPrincipal();
		up.parentId=parentFolder.getId();
		return up;
	}
	
	public static UploadPrincipal createForFileVersionUpload(GenericFile<?> file){
		UploadPrincipal up=new UploadPrincipal();
		up.fileId=file.getId();
		return up;
	}
}
