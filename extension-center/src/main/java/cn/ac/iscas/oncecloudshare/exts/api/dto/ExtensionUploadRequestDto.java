package cn.ac.iscas.oncecloudshare.exts.api.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public class ExtensionUploadRequestDto {
	@NotNull
	@Size(min = 1, max = 32)
	private String name;
	@NotNull
	@Size(min = 1, max = 32)
	private String version;
	@Size(max = 1024)
	private String description;
	@Size(max = 32)
	private String minSupport;
	@Size(max = 32)
	private String maxSupport;
	@NotNull
	private MultipartFile file;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}
}
