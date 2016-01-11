package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import java.util.Date;

import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.dto.space.TagDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;

import com.google.common.base.Function;

public class SpaceFileDto {
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
	// 最新版本
	public SpaceFileVersionDto headVersion;
	// 上传者
	public UserDto creator;

	public Integer childrenCount;

	// 工作空间相关信息
	public long workspaceId;
	public String workspaceName;

	// 标签
	public TagDto[] tags;
	
	// 下载次数和收藏次数
	public long follows;
	public long downloads;

	public static Function<SpaceFile, SpaceFileDto> defaultTransformer = new Function<SpaceFile, SpaceFileDto>() {
		@Override
		public SpaceFileDto apply(SpaceFile input) {
			if (input == null) {
				return null;
			}
			SpaceFileDto dto = new SpaceFileDto();
			dto.id = input.getId();
			dto.name = input.getName();
			dto.path = input.getPath();
			dto.ownerId = input.getOwner().getId();
			if (input.getParent() != null) {
				dto.parentId = input.getParent().getId();
			}
			dto.isDir = input.getIsDir();
			if (dto.isDir) {
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
				dto.headVersion = SpaceFileVersionDto.defaultTransformer.apply(input.getHeadVersion());
			}
			dto.creator = UserDto.GLANCE_TRANSFORMER.apply(input.getCreator());

			// 工作空间的相关信息
			if (input.getOwner() != null && input.getOwner() instanceof WorkspaceSpace) {
				WorkspaceSpace space = (WorkspaceSpace) input.getOwner();
				if (space.getWorkspace() != null) {
					dto.workspaceId = space.getWorkspace().getId();
					dto.workspaceName = space.getWorkspace().getName();
				}
			}

			if (Collections3.isNotEmpty(input.getTags())) {
				dto.tags = new TagDto[input.getTags().size()];
				for (int i = 0, ii = input.getTags().size(); i < ii; i++) {
					dto.tags[i] = TagDto.Transformers.DEFAULT.apply(input.getTags().get(i));
				}
			}
			
			dto.downloads = input.getDownloads();
			dto.follows = input.getFollows();

			return dto;
		}
	};

	public static Function<SpaceFile, SpaceFileDto> indexTransformer = new Function<SpaceFile, SpaceFileDto>() {
		@Override
		public SpaceFileDto apply(SpaceFile input) {
			if (input == null) {
				return null;
			}
			SpaceFileDto dto = new SpaceFileDto();
			dto.id = input.getId();
			dto.name = input.getName();
			dto.path = input.getPath();
			dto.ownerId = input.getOwner().getId();
			if (input.getParent() != null) {
				dto.parentId = input.getParent().getId();
			}
			dto.isDir = input.getIsDir();
			if (dto.isDir) {
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
				dto.headVersion = SpaceFileVersionDto.defaultTransformer.apply(input.getHeadVersion());
			}
			dto.creator = UserDto.GLANCE_TRANSFORMER.apply(input.getCreator());

			// 工作空间的相关信息
			if (input.getOwner() != null && input.getOwner() instanceof WorkspaceSpace) {
				WorkspaceSpace space = (WorkspaceSpace) input.getOwner();
				if (space.getWorkspace() != null) {
					dto.workspaceId = space.getWorkspace().getId();
					dto.workspaceName = space.getWorkspace().getName();
				}
			}

			return dto;
		}
	};

	public static Function<SpaceFileFollow, SpaceFileDto> followTransformer = new Function<SpaceFileFollow, SpaceFileDto>() {
		public SpaceFileDto apply(SpaceFileFollow input) {
			SpaceFileDto dto = defaultTransformer.apply(input.getFile());
			dto.favorite = true;
			return dto;
		}
	};
}
