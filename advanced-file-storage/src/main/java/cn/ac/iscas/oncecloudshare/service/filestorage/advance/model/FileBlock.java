package cn.ac.iscas.oncecloudshare.service.filestorage.advance.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.security.Encryptions;
import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

/**
 * 文件块存储信息
 * 
 * @author Chen Hao
 */
@Entity
@Table(name = "ocs_file_block")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FileBlock extends IdEntity {

	protected String md5;

	protected Long size;

	protected String location;

	private StorageDevice storageDevice;

	private Encryptions encryption;

	@Column(nullable = false, unique = true, columnDefinition = "CHAR(32)")
	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@Column(nullable = false, columnDefinition = "BIGINT(15)")
	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	/**
	 * 用uri的方式表示block的实际存储位置
	 * 
	 * @return
	 */
	@Column(nullable = false, length = 256)
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@ManyToOne
	@JoinColumn(name = "device_id", referencedColumnName = "id", nullable = false)
	public StorageDevice getStorageDevice() {
		return storageDevice;
	}

	public void setStorageDevice(StorageDevice storageDevice) {
		this.storageDevice = storageDevice;
	}

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "CHAR(32) NOT NULL DEFAULT 'NULL'")
	public Encryptions getEncryption() {
		return encryption;
	}

	public void setEncryption(Encryptions encryption) {
		this.encryption = encryption;
	}

}
