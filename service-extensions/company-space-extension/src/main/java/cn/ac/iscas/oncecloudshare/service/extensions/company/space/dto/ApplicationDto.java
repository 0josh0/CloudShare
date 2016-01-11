package cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.UploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.UploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;

import com.google.common.base.Function;

public class ApplicationDto {
	public Long id;
	public String type;
	public String description;
	// 状态
	public String status;
	// 申请人
	public UserDto applicant;
	// 申请时间
	public Long createTime;
	// 审核人
	public UserDto reviewer;
	// 审核时间
	public Long reviewTime;
	// 审核的意见
	public String reviewMessage;
	
	// 上传的文件
	public SpaceFileDto uploadedFile;
	// 上传到的目录
	public SpaceFileDto targetFolder;
	// 覆盖的文件
	public SpaceFileDto targetFile;

	public static Function<CompanySpaceApplication, ApplicationDto> DEFAULT_TRANSFORMER = new Function<CompanySpaceApplication, ApplicationDto>() {
		@Override
		public ApplicationDto apply(CompanySpaceApplication application) {
			if (application == null) {
				return null;
			}
			ApplicationDto output = new ApplicationDto();
			output.id = application.getId();
			output.description = application.getDescription();
			// 申请相关信息
			output.applicant = UserDto.GLANCE_TRANSFORMER.apply(application.getApplicant());
			if (application.getCreateTime() != null) {
				output.createTime = application.getCreateTime().getTime();
			}
			// 审核相关信息
			output.reviewer = UserDto.GLANCE_TRANSFORMER.apply(application.getReviewBy());
			if (application.getReviewTime() != null) {
				output.reviewTime = application.getReviewTime().getTime();
			}
			output.reviewMessage = application.getReviewMessage();
			// 状态
			output.status = application.getStatus() == null ? null : application.getStatus().name();
			
			output.type = application.getClass().getSimpleName();
			
			// 如果是上传文件
			if (application instanceof UploadApplication){
				output.uploadedFile = SpaceFileDto.defaultTransformer.apply(((UploadApplication) application).getUploadedFile());
				output.targetFolder = SpaceFileDto.defaultTransformer.apply(((UploadApplication) application).getTargetFolder());
			} 
			// 如果是上传文件版本
			else if (application instanceof UploadVersionApplication){
				output.uploadedFile = SpaceFileDto.defaultTransformer.apply(((UploadVersionApplication) application).getUploadedFile());
				output.targetFile = SpaceFileDto.defaultTransformer.apply(((UploadVersionApplication) application).getTargetFile());
			}
			
			return output;
		}
	};
	
	public static class UploadReview{
		// 是否通过，默认通过
		private Boolean agreed = Boolean.TRUE;
		// 目标文件夹id
		private Long parentId;
		// 名称
		@Pattern(regexp = FilePathUtil.VALID_FILE_NAME_REGEX)
		private String name;
		// 审核意见
		@Length(max = 255)
		private String message;

		public Boolean getAgreed() {
			return agreed;
		}

		public void setAgreed(Boolean agreed) {
			this.agreed = agreed;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

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
	
	public static class UploadVersionReview{
		// 是否通过，默认通过
		private Boolean agreed = Boolean.TRUE;
		// 审核意见
		@Length(max = 255)
		private String message;

		public Boolean getAgreed() {
			return agreed;
		}

		public void setAgreed(Boolean agreed) {
			this.agreed = agreed;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}
