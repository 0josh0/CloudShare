package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

public class UploadFile {
	private long parentId;
	private String name;
	// 临时文件的key值
	private String tfKey;
	// 文件的md5值
	private String fileMd5;

	public static UploadFile fromTempFile(long parentId, String name, String tfKey) {
		UploadFile request = new UploadFile();
		request.setParentId(parentId);
		request.setName(name);
		request.setTfKey(tfKey);
		return request;
	}

	public static UploadFile fromMd5(long parentId, String name, String md5) {
		UploadFile request = new UploadFile();
		request.setParentId(parentId);
		request.setName(name);
		request.setFileMd5(md5);
		return request;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTfKey() {
		return tfKey;
	}

	public void setTfKey(String tfKey) {
		this.tfKey = tfKey;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}
}
