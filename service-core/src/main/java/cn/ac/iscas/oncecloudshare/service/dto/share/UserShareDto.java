package cn.ac.iscas.oncecloudshare.service.dto.share;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.model.share.UserShare;

import com.google.common.base.Function;

public class UserShareDto {
	static final Logger _logger = LoggerFactory.getLogger(UserShareDto.class);

	public static class OwnerView {
		// 用户分享ID
		public long id;
		// 创建时间
		public Long createTime;
		// 描述
		public String description;
		// 接受者ID
		public Long recipientId;
		// 接受者用户名
		public String recipientName;

		// 文件相关信息
		public Long fileId;
		public Long fileSize;
		public String fileName;
		public Integer fileVersion;
		public String fileMd5;
	}

	public static class RecipientView {
		// 用户分享ID
		public long id;
		// 创建时间
		public Long createTime;
		// 描述
		public String description;
		// 接受者ID
		public Long sharerId;
		// 接受者用户名
		public String sharerName;

		// 文件相关信息
		public Long fileId;
		public Long fileSize;
		public String fileName;
		public Integer fileVersion;
		public String fileMd5;
	}

	public static class Create {
		public Long fileId;
		public String description;
		public Long[] recipients;
		public Boolean shareHeadVersion = Boolean.TRUE;

		public Long getFileId() {
			return fileId;
		}

		public void setFileId(Long fileId) {
			this.fileId = fileId;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Long[] getRecipients() {
			return recipients;
		}

		public void setRecipients(Long[] recipients) {
			this.recipients = recipients;
		}

		public Boolean getShareHeadVersion() {
			return shareHeadVersion;
		}

		public void setShareHeadVersion(Boolean shareHeadVersion) {
			this.shareHeadVersion = shareHeadVersion;
		}
	}

	public static Function<UserShare, OwnerView> ownerViewTransformer = new Function<UserShare, OwnerView>() {
		@Override
		public OwnerView apply(UserShare input) {
			if (input == null) {
				return null;
			}
			OwnerView dto = new OwnerView();

			dto.id = input.getId();
			if (input.getCreateTime() != null) {
				dto.createTime = input.getCreateTime().getTime();
			}
			dto.description = input.getDescription();
			if (input.getRecipient() != null) {
				dto.recipientId = input.getRecipient().getId();
				dto.recipientName = input.getRecipient().getName();
			}
			// 文件相关信息
			if (input.getFile() != null) {
				dto.fileId = input.getFile().getId();
				dto.fileName = input.getFile().getName();
				FileVersion fileVersion = null;
				if (input.isShareHeadVersion()) {
					fileVersion = input.getFile().getHeadVersion();
				} else {
					fileVersion = input.getFile().getVersion(input.getFileVersion());
					dto.fileVersion = input.getFileVersion();
				}
				if (fileVersion == null) {
					_logger.warn("外链分享{}不存在对应的文件版本！", input.getId());
				} else {
					dto.fileSize = fileVersion.getSize();
					dto.fileMd5 = fileVersion.getMd5();
				}
			} else {
				_logger.warn("外链分享{}不存在对应的文件！", input.getId());
			}

			return dto;
		}
	};

	public static Function<UserShare, RecipientView> recipientViewTransformer = new Function<UserShare, RecipientView>() {
		@Override
		public RecipientView apply(UserShare input) {
			if (input == null) {
				return null;
			}
			RecipientView dto = new RecipientView();

			dto.id = input.getId();
			if (input.getCreateTime() != null) {
				dto.createTime = input.getCreateTime().getTime();
			}
			dto.description = input.getDescription();
			if (input.getOwner() != null) {
				dto.sharerId = input.getOwner().getId();
				dto.sharerName = input.getOwner().getName();
			}
			// 文件相关信息
			if (input.getFile() != null) {
				dto.fileId = input.getFile().getId();
				dto.fileName = input.getFile().getName();
				FileVersion fileVersion = null;
				if (input.isShareHeadVersion()) {
					fileVersion = input.getFile().getHeadVersion();
				} else {
					fileVersion = input.getFile().getVersion(input.getFileVersion());
					dto.fileVersion = input.getFileVersion();
				}
				if (fileVersion == null) {
					_logger.warn("外链分享{}不存在对应的文件版本！", input.getId());
				} else {
					dto.fileSize = fileVersion.getSize();
					dto.fileMd5 = fileVersion.getMd5();
				}
			} else {
				_logger.warn("外链分享{}不存在对应的文件！", input.getId());
			}

			return dto;
		}
	};
}
