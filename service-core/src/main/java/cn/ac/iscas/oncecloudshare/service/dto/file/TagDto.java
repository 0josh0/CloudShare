package cn.ac.iscas.oncecloudshare.service.dto.file;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.Tag;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class TagDto {
	public Long id;
	public String title;
	public Integer orderIndex;
	public Long filesCount;
	public UserDto owner;
	public Long createTime;

	public static final Function<Tag, TagDto> DEFAULT_TRANSFORMER = new Function<Tag, TagDto>() {
		@Override
		public TagDto apply(Tag input) {
			Preconditions.checkNotNull(input);
			TagDto output = new TagDto();
			output.id = input.getId();
			output.title = input.getTitle();
			output.orderIndex = input.getOrderIndex();
			output.filesCount = input.getFilesCount();
			if (input.getCreateTime() != null) {
				output.createTime = input.getCreateTime().getTime();
			}
			return output;
		}
	};
}
