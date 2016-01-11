package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileComment;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class FileCommentDto {

	public UserDto user;

	public String content;

	public long createTime;

	public SpaceFileDto file;

	public long id;
	
	public List<UserDto> at;

	public static Function<FileComment, FileCommentDto> defaultTransformer = new Function<FileComment, FileCommentDto>() {
		@Override
		public FileCommentDto apply(FileComment input) {
			if (input == null) {
				return null;
			}
			FileCommentDto dto = new FileCommentDto();

			dto.user = UserDto.GLANCE_TRANSFORMER.apply(input.getCreater());

			dto.file = SpaceFileDto.defaultTransformer.apply(input.getFile());

			dto.id = input.getId();
			dto.content = input.getContent();
			dto.createTime = input.getCreateTime().getTime();
			
			if (input.getAt() != null && input.getAt().size() > 0){
				dto.at = Lists.transform(input.getAt(), UserDto.GLANCE_TRANSFORMER);
			}
			
			return dto;
		}
	};

}
