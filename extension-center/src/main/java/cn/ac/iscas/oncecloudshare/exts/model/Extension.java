package cn.ac.iscas.oncecloudshare.exts.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "oce_extension", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "version" }) })
public class Extension extends IdEntity {
	private String name;
	private String version;
	private String description;
	private String minSupport;
	private String maxSupport;
	private String filePath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(String minSupport) {
		this.minSupport = minSupport;
	}

	public String getMaxSupport() {
		return maxSupport;
	}

	public void setMaxSupport(String maxSupport) {
		this.maxSupport = maxSupport;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
