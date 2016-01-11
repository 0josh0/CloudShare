package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileOwner;

@Entity
@Table(name = "ocs_space")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 30)
public class BaseSpace extends IdEntity implements FileOwner {
	// 配额
	private Long quota;
	// 剩余的配额
	private Long restQuota;
	// 文件夹的个数
	private Long foldsCount;
	// 文件的个数
	private Long filesCount;

	public Long getQuota() {
		return quota;
	}

	public void setQuota(Long quota) {
		this.quota = quota;
	}

	@Column(updatable = false)
	public Long getRestQuota() {
		return restQuota;
	}

	public void setRestQuota(Long restQuota) {
		this.restQuota = restQuota;
	}

	@Column(updatable = false)
	public Long getFoldsCount() {
		return foldsCount;
	}

	public void setFoldsCount(Long foldsCount) {
		this.foldsCount = foldsCount;
	}

	@Column(updatable = false)
	public Long getFilesCount() {
		return filesCount;
	}

	public void setFilesCount(Long filesCount) {
		this.filesCount = filesCount;
	}
	
	@Transient
	public boolean hasFile(SpaceFile file){
		return file != null && file.getOwner().getId().equals(getId());
	}
}