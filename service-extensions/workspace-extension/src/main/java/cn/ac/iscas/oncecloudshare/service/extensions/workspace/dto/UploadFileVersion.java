package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

public class UploadFileVersion {
	private long fileId;
	// 临时文件的key值
	private String tfKey;
	// 文件的md5值
	private String fileMd5;

	public static UploadFileVersion formTempFile(long fileId, String tfKey) {
		UploadFileVersion request = new UploadFileVersion();
		request.setFileId(fileId);
		request.setTfKey(tfKey);
		return request;
	}

	public static UploadFileVersion formMd5(long fileId, String md5) {
		UploadFileVersion request = new UploadFileVersion();
		request.setFileId(fileId);
		request.setFileMd5(md5);
		return request;
	}

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
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
