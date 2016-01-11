package cn.ac.iscas.oncecloudshare.service.extensions.backup.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "ocs_backup")
public class Backup extends IdEntity {
	private String filePath;
	private String fileName;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
