package cn.ac.iscas.oncecloudshare.service.extensions.company.space.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;

/**
 * 上传文件版本申请
 * 
 * @author cly
 * @version  
 * @since JDK 1.6
 */
@Entity
@DiscriminatorValue("upload_version")
public class UploadVersionApplication extends CompanySpaceApplication {
	// 要上传的文件
	private SpaceFile uploadedFile;
	// 上传的目标路径
	private SpaceFile targetFile;

	@ManyToOne
	@JoinColumn(name = "uploaded_file_id")
	public SpaceFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(SpaceFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	@ManyToOne
	@JoinColumn(name = "target_file_id")
	public SpaceFile getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(SpaceFile targetFile) {
		this.targetFile = targetFile;
	}
}
