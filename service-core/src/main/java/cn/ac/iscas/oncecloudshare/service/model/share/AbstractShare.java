package cn.ac.iscas.oncecloudshare.service.model.share;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;

@MappedSuperclass
public abstract class AbstractShare extends DescriptionEntity {
	// 要分享的文件
	private File file;
	// 分享的文件版本
	private Integer fileVersion;
	// 分享操作的执行人
	private User owner;

	@ManyToOne
	@JoinColumn(name = "file_id", nullable = false, updatable = false)
	public File getFile() {
		return file;
	}

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false, updatable = false)
	public User getOwner() {
		return owner;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setOwner(User user) {
		this.owner = user;
	}

	public Integer getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(Integer fileVersion) {
		this.fileVersion = fileVersion;
	}

	@Transient
	public boolean isShareHeadVersion() {
		return fileVersion == null ? true : false;
	}

	public void setShareHeadVersion() {
		this.fileVersion = null;
	}
	
	@Transient
	public FileVersion getSharedFileVersion(){
		return isShareHeadVersion() ? file.getHeadVersion() : file.getVersion(getFileVersion());
	}
}