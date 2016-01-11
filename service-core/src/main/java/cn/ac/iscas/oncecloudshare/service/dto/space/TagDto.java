package cn.ac.iscas.oncecloudshare.service.dto.space;

import cn.ac.iscas.oncecloudshare.service.model.common.SpaceTag;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class TagDto {
	public Long id;
	public String title;
	public Integer orderIndex;
	public Long filesCount;
	public Long createTime;

	public static class Transformers{
		public static final Function<SpaceTag, TagDto> DEFAULT = new Function<SpaceTag, TagDto>() {
			@Override
			public TagDto apply(SpaceTag input) {
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
}
