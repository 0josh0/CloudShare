package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import java.util.Date;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;

import com.google.common.base.Function;

public class SpaceFileVersionDto {
	public Integer version;
	public Long size;
	public String md5;
	public Date createTime;
	public Date updateTime;
	public UserDto uploader;

	public static Function<SpaceFileVersion, SpaceFileVersionDto> defaultTransformer = new Function<SpaceFileVersion, SpaceFileVersionDto>() {
		@Override
		public SpaceFileVersionDto apply(SpaceFileVersion input) {
			if (input == null) {
				return null;
			}
			SpaceFileVersionDto dto = new SpaceFileVersionDto();
			dto.version = input.getVersion();
			dto.size = input.getSize();
			dto.md5 = input.getMd5();
			dto.createTime = input.getCreateTime();
			dto.updateTime = input.getUpdateTime();
			dto.uploader = UserDto.GLANCE_TRANSFORMER.apply(input.getCreator());
			return dto;
		}
	};
}
