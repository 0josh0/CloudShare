package cn.ac.iscas.oncecloudshare.service.extensions.company.space.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;

/**
 * 上传文件申请
 * 
 * @author cly
 * @version  
 * @since JDK 1.6
 */
@Entity
@DiscriminatorValue("upload")
public class UploadApplication extends CompanySpaceApplication {
	// 要上传的文件
	private SpaceFile uploadedFile;
	// 上传的目标路径
	private SpaceFile targetFolder;

	@ManyToOne
	@JoinColumn(name = "uploaded_file_id")
	public SpaceFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(SpaceFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	@ManyToOne
	@JoinColumn(name = "target_folder_id")
	public SpaceFile getTargetFolder() {
		return targetFolder;
	}

	public void setTargetFolder(SpaceFile targetFolder) {
		this.targetFolder = targetFolder;
	}
}
