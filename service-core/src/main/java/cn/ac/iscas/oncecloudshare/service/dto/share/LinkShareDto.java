package cn.ac.iscas.oncecloudshare.service.dto.share;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.model.share.LinkShare;

import com.google.common.base.Function;

public class LinkShareDto {
	static final Logger _logger = LoggerFactory.getLogger(LinkShareDto.class);

	public static class Request {
		// 文件编号
		private Long fileId;
		// 提取码
		private String pass;
		// 过期时间
		private Long expireTime;
		//
		private Boolean shareHeadVersion = Boolean.TRUE;
		// 描述
		private String description;

		public Long getFileId() {
			return fileId;
		}

		public void setFileId(Long fileId) {
			this.fileId = fileId;
		}

		public String getPass() {
			return pass;
		}

		public void setPass(String pass) {
			this.pass = pass;
		}

		public Long getExpireTime() {
			return expireTime;
		}

		public void setExpireTime(Long expireTime) {
			this.expireTime = expireTime;
		}

		public Boolean getShareHeadVersion() {
			return shareHeadVersion;
		}

		public void setShareHeadVersion(Boolean shareHeadVersion) {
			this.shareHeadVersion = shareHeadVersion;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class OwnerView {
		public long id;
		public String key;
		public String pass;
		public Long expireTime;
		public Long createTime;
		public Long downloads;
		public String description;

		public String url;

		// 文件相关信息
		public Long fileId;
		public Long fileSize;
		public String fileName;
		public Integer fileVersion;
		public String fileMd5;

		public static Function<LinkShare, LinkShareDto.OwnerView> TRANSFORMER = new Function<LinkShare, LinkShareDto.OwnerView>() {
			@Override
			public LinkShareDto.OwnerView apply(LinkShare input) {
				if (input == null) {
					return null;
				}
				LinkShareDto.OwnerView dto = new OwnerView();

				dto.id = input.getId();
				dto.key = input.getKey();
				dto.pass = input.getPass();
				if (!input.isNeverExpired()) {
					dto.expireTime = input.getExpireTime().getTime();
				}
				if (input.getCreateTime() != null) {
					dto.createTime = input.getCreateTime().getTime();
				}
				dto.downloads = input.getDownloads();
				dto.description = input.getDescription();
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

		public static LinkShareDto.OwnerView of(LinkShare sharedLink) {
			return TRANSFORMER.apply(sharedLink);
		}
	}

	public static class AnonView {
		public long id;
		public String key;
		public boolean needPass;
		public Long expireTime;
		public long createTime;
		public long downloads;
		public String description;

		// 文件相关信息
		public Long fileId;
		public Long fileSize;
		public String fileName;
		public Integer fileVersion;
		public String fileMd5;

		public static Function<LinkShare, AnonView> TRANSFORMER = new Function<LinkShare, AnonView>() {
			@Override
			public AnonView apply(LinkShare input) {
				if (input == null) {
					return null;
				}
				AnonView dto = new AnonView();

				dto.id = input.getId();
				dto.key = input.getKey();
				dto.needPass = input.isNeedPass();
				if (!input.isNeverExpired()) {
					dto.expireTime = input.getExpireTime().getTime();
				}
				if (input.getCreateTime() != null) {
					dto.createTime = input.getCreateTime().getTime();
				}
				dto.downloads = input.getDownloads();
				dto.description = input.getDescription();
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

		public static AnonView of(LinkShare sharedLink) {
			return TRANSFORMER.apply(sharedLink);
		}
	}
}
