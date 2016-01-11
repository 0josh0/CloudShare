package cn.ac.iscas.oncecloudshare.service.model.share;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;

@Entity
@Table(name = "ocs_share")
public class Share extends DescriptionEntity {
	public static enum Status {
		CREATED, CANCELED;
	}

	// 要分享的文件
	private File file;
	// 分享的文件版本
	private Integer fileVersion;
	// 创建分享的用户
	private User creator;
	// 分享的状态
	private Status status;
	// 撤销分享的时间
	private Date cancelTime;
	// 分享的目标
	private List<ShareRecipient> recipients;

	@ManyToOne
	@JoinColumn(name = "file_id", nullable = false)
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Integer getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(Integer fileVersion) {
		this.fileVersion = fileVersion;
	}

	@ManyToOne
	@JoinColumn(name = "create_by", nullable = false)
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	@Column(nullable = false, length = 16)
	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Transient
	public boolean isShareHeadVersion() {
		return fileVersion == null;
	}

	public void setShareHeadVersion() {
		this.fileVersion = null;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getCancelTime() {
		return cancelTime;
	}

	public void setCancelTime(Date cancelTime) {
		this.cancelTime = cancelTime;
	}

	@OneToMany(mappedBy = "share", cascade = CascadeType.PERSIST)
	public List<ShareRecipient> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<ShareRecipient> recipients) {
		this.recipients = recipients;
	}

	@Transient
	public FileVersion getSharedFileVersion() {
		if (isShareHeadVersion()){
			return file.getHeadVersion();
		} else {
			return file.getVersion(fileVersion);
		}
	}
}