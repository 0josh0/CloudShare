package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

public class TempFile {
	private String key;
	private long expireAt;

	public TempFile(String key, long expireAt) {
		super();
		this.key = key;
		this.expireAt = expireAt;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(long expireAt) {
		this.expireAt = expireAt;
	}
}
