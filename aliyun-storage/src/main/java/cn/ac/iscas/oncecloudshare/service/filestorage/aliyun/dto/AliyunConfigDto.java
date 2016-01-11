package cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AliyunConfigDto {
	@NotNull
	@Size(max = 255)
	private String key;
	@NotNull
	@Size(max = 255)
	private String secret;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

}
