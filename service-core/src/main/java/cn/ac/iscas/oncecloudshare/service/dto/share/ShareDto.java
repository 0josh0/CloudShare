package cn.ac.iscas.oncecloudshare.service.dto.share;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.model.share.ShareRecipient;

import com.google.common.base.Function;

public class ShareDto {
	private static final Logger _logger = LoggerFactory.getLogger(ShareDto.class);

	public static class Brief {
		// 用户分享ID
		public long id;
		// 创建时间
		public Long createTime;
		// 描述
		public String message;

		public Recipient[] recipients;

		// 文件相关信息
		public Long fileId;
		public Long fileSize;
		public String fileName;
		public Long fileVersionId;
		public Integer fileVersion;
		public String fileMd5;
		public Boolean isDir;
	}

	/**
	 * 创建分享时使用的DTO
	 */
	public static class CreateRequest {
		// 分享的目标
		@Size(min = 1)
		public String[] recipients;
		// 分享的文件id
		@NotNull
		public Long fileId;
		// 分享的消息
		@Length(max = 255)
		public String message;
		// 是否分享最新版本
		public boolean shareHeadVersion = true;

		public String[] getRecipients() {
			return recipients;
		}

		public void setRecipients(String[] recipients) {
			this.recipients = recipients;
		}

		public Long getFileId() {
			return fileId;
		}

		public void setFileId(Long fileId) {
			this.fileId = fileId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public boolean isShareHeadVersion() {
			return shareHeadVersion;
		}

		public void setShareHeadVersion(boolean shareHeadVersion) {
			this.shareHeadVersion = shareHeadVersion;
		}
	}

	public static class Recipient {
		public Recipient(ShareRecipient shareRecipient) {
			this.type = shareRecipient.getType();
			this.identify = shareRecipient.getIdentify();
			this.displayName = shareRecipient.getDisplayName();
		}

		public Recipient() {
		}

		@NotNull
		public String type;
		@NotNull
		public Long identify;
		public String displayName;
	}

	public static Function<Share, Brief> toBrief = new Function<Share, Brief>() {
		public Brief apply(Share input) {
			if (input == null) {
				return null;
			}
			Brief dto = new Brief();

			dto.id = input.getId();
			if (input.getCreateTime() != null) {
				dto.createTime = input.getCreateTime().getTime();
			}
			dto.message = input.getDescription();
			if (input.getRecipients() != null && input.getRecipients().size() > 0) {
				Recipient[] recipients = new Recipient[input.getRecipients().size()];
				for (int i = 0, ii = recipients.length; i < ii; i++) {
					recipients[i] = new Recipient(input.getRecipients().get(i));
				}
				dto.recipients = recipients;
			}
			// 文件相关信息
			if (input.getFile() != null) {
				dto.fileId = input.getFile().getId();
				dto.fileName = input.getFile().getName();
				dto.isDir = input.getFile().getIsDir();
				FileVersion fileVersion = null;
				if (input.isShareHeadVersion()) {
					fileVersion = input.getFile().getHeadVersion();
				} else {
					fileVersion = input.getFile().getVersion(input.getFileVersion());
					dto.fileVersion = input.getFileVersion();
				}
				if (fileVersion !=null) {
					dto.fileVersionId = fileVersion.getId();
					dto.fileSize = fileVersion.getSize();
					dto.fileMd5 = fileVersion.getMd5();
				}
			} else {
				_logger.warn("分享{}不存在对应的文件！", input.getId());
			}

			return dto;
		}
	};

	public static Function<String, ShareRecipient> dtoToRecipient = new Function<String, ShareRecipient>() {
		public ShareRecipient apply(String input) {
			if (input == null) {
				return null;
			}
			String[] array = input.split("#");
			if (array.length != 2) {
				return null;
			}
			String type = array[0];
			long identify = NumberUtils.toLong(array[1], Long.MIN_VALUE);
			ShareRecipient result = new ShareRecipient();
			result.setType(type);
			result.setIdentify(identify);
			if (identify == Long.MIN_VALUE) {
				result.setDisplayName(array[1]);
			}
			return result;
		}
	};
}
