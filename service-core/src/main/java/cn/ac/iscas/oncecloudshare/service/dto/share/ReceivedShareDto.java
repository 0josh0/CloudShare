package cn.ac.iscas.oncecloudshare.service.dto.share;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.ShareRecipient;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ReceivedShareDto {
	private static final Logger _logger = LoggerFactory.getLogger(ReceivedShareDto.class);
	
	public static class Brief {
		// 编号
		public long id;
		// 接受者信息
		public List<Recipient> recipients;
		// 发送的信息
		public String message;
		// 创建者信息
		public Long creatorId;
		public String creatorName;
		// 创建时间
		public Long createTime;
		// 分享的文件信息
		public Long fileId;
		public Long fileSize;
		public String fileName;
		public String filePath;
		public Integer fileVersion;
		public String fileMd5;
		public Boolean isDir;
	}

	public static Function<ReceivedShare, Brief> toBrief = new Function<ReceivedShare, Brief>() {
		public Brief apply(ReceivedShare input) {
			Brief result = new Brief();
			result.id = input.getId();
			if (input.getShare() != null) {
				result.message = input.getShare().getDescription();
				// 发送者信息
				if (input.getShare().getCreator() != null) {
					result.creatorId = input.getShare().getCreator().getId();
					result.creatorName = input.getShare().getCreator().getName();
				}
				if (input.getCreateTime() != null){
					result.createTime = input.getCreateTime().getTime();
				}
				// 接受者信息
				if (input.getShare().getRecipients() != null && input.getShare().getRecipients().size() > 0) {
					result.recipients = Lists.newArrayList();
					for (ShareRecipient recipient : input.getShare().getRecipients()) {
						Recipient tmp = new Recipient();
						tmp.type = recipient.getType();
						tmp.identify = recipient.getIdentify();
						tmp.displayName = recipient.getDisplayName();
						if (input.getBelongsTo().indexOf(recipient) != -1) {
							tmp.belongsTo = true;
						}
						result.recipients.add(tmp);
					}
				}
				if (input.getShare().getFile() != null){
					result.fileId = input.getShare().getFile().getId();
					result.fileName = input.getShare().getFile().getName();
					result.filePath = input.getShare().getFile().getPath();
					result.isDir = input.getShare().getFile().getIsDir();
					FileVersion fileVersion = null;
					if (input.getShare().isShareHeadVersion()) {
						fileVersion = input.getShare().getFile().getHeadVersion();
					} else {
						fileVersion = input.getShare().getFile().getVersion(input.getShare().getFileVersion());
						result.fileVersion = input.getShare().getFileVersion();
					}
					if (fileVersion == null) {
						_logger.warn("分享{}不存在对应的文件版本！", input.getId());
					} else {
						result.fileSize = fileVersion.getSize();
						result.fileMd5 = fileVersion.getMd5();
					}
				}
			}
			return result;
		}
	};

	public static class Recipient {
		public String type;
		public Long identify;
		public String displayName;
		public Boolean belongsTo;
	}

	public static class File {
		// 文件信息
		public Long id;
		public String name;
		public Long ownerId;
		public Long parentId;
		public Boolean isDir;
		// version信息
		public String mimeType;
		public Long size;
		public String md5;
	}
	
	public static Function<ReceivedShare, Brief> toNotify = new Function<ReceivedShare, Brief>() {
		public Brief apply(ReceivedShare input) {
			Brief result = new Brief();
			result.id = input.getId();
			if (input.getShare() != null) {
				result.message = input.getShare().getDescription();
				// 发送者信息
				if (input.getShare().getCreator() != null) {
					result.creatorId = input.getShare().getCreator().getId();
					result.creatorName = input.getShare().getCreator().getName();
				}
				if (input.getCreateTime() != null){
					result.createTime = input.getCreateTime().getTime();
				}
				if (input.getShare().getFile() != null){
					result.fileId = input.getShare().getFile().getId();
					result.fileName = input.getShare().getFile().getName();
					result.isDir = input.getShare().getFile().getIsDir();
					FileVersion fileVersion = null;
					if (input.getShare().isShareHeadVersion()) {
						fileVersion = input.getShare().getFile().getHeadVersion();
					} else {
						fileVersion = input.getShare().getFile().getVersion(input.getShare().getFileVersion());
						result.fileVersion = input.getShare().getFileVersion();
					}
					if (fileVersion == null) {
						_logger.warn("分享{}不存在对应的文件版本！", input.getId());
					} else {
						result.fileSize = fileVersion.getSize();
						result.fileMd5 = fileVersion.getMd5();
					}
				}
			}
			return result;
		}
	};
}
