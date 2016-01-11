package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import javax.validation.constraints.Pattern;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;

public class ReviewUpload extends ReviewApplication {
	// 目标文件夹id
	private Long parentId;
	// 名称
	@Pattern(regexp = FilePathUtil.VALID_FILE_NAME_REGEX)
	private String name;

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
