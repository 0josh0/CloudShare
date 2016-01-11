package cn.ac.iscas.oncecloudshare.service.dto.file;

import java.util.Date;

import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import com.google.common.base.Function;

public class FileDto {

	public static Function<File, FileDto> TRANSFORMER = new Function<File, FileDto>() {

		@Override
		public FileDto apply(File input) {
			if (input == null) {
				return null;
			}
			FileDto dto = new FileDto();
			dto.id = input.getId();
			dto.name = input.getName();
			dto.path = input.getPath();
			dto.ownerId = input.getOwner().getId();
			if (input.getParent() != null) {
				dto.parentId = input.getParent().getId();
			}
			dto.isDir = input.getIsDir();
			if (input.getIsDir()) {
				dto.childrenCount = input.getChildrenCount(FileStatus.HEALTHY);
			}
			dto.favorite = input.getFavorite();
			dto.mimeType = input.getMimeType();
			dto.status = input.getStatus();
			dto.description = input.getDescription();
			dto.createTime = input.getCreateTime();
			dto.updateTime = input.getUpdateTime();
			if (Collections3.isNotEmpty(input.getVersions())) {
				dto.versionCount = input.getVersions().size();
				dto.headVersion = FileVersionDto.of(input.getHeadVersion());
			}
			if (Collections3.isNotEmpty(input.getTags())) {
				dto.tags = new TagDto[input.getTags().size()];
				for (int i = 0, ii = input.getTags().size(); i < ii; i++) {
					dto.tags[i] = TagDto.DEFAULT_TRANSFORMER.apply(input.getTags().get(i));
				}
			}
			return dto;
		}
	};

	public static Function<File, FileDto> INDEX_TRANSFORMER = new Function<File, FileDto>() {

		@Override
		public FileDto apply(File input) {
			if (input == null) {
				return null;
			}
			FileDto dto = new FileDto();
			dto.id = input.getId();
			dto.name = input.getName();
			dto.path = input.getPath();
			dto.ownerId = input.getOwner().getId();
			if (input.getParent() != null) {
				dto.parentId = input.getParent().getId();
			}
			dto.isDir = input.getIsDir();
			if (input.getIsDir()) {
				dto.childrenCount = input.getChildrenCount(FileStatus.HEALTHY);
			}
			dto.favorite = input.getFavorite();
			dto.mimeType = input.getMimeType();
			dto.status = input.getStatus();
			dto.description = input.getDescription();
			dto.createTime = input.getCreateTime();
			dto.updateTime = input.getUpdateTime();
			if (Collections3.isNotEmpty(input.getVersions())) {
				dto.versionCount = input.getVersions().size();
				dto.headVersion = FileVersionDto.of(input.getHeadVersion());
			}
			return dto;
		}
	};

	public Long id;
	public String name;
	public String path;
	public Long ownerId;
	public Long parentId;
	public Boolean isDir;
	public Boolean favorite;
	public String mimeType;
	public FileStatus status;
	public String description;
	public Date createTime;
	public Date updateTime;
	public Integer versionCount;
	public FileVersionDto headVersion;
	public Integer childrenCount;
	public TagDto[] tags;

	public static FileDto of(File file) {
		return TRANSFORMER.apply(file);
	}

	public static FileDto ofIndex(File file) {
		return INDEX_TRANSFORMER.apply(file);
	}
}
