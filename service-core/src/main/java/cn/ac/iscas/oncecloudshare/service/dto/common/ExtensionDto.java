package cn.ac.iscas.oncecloudshare.service.dto.common;

import org.apache.commons.lang3.StringUtils;

import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.model.common.ExtensionInfo;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class ExtensionDto extends ExtensionInfo{	
	public static Function<String, PageDto<ExtensionInfo>> pageDecoder = new Function<String, PageDto<ExtensionInfo>>() {
		public PageDto<ExtensionInfo> apply(String input){
			if (StringUtils.isEmpty(input)){
				return null;
			}
			Gson gson = Gsons.defaultGson();
			PageDto<?> tmpPage = gson.fromJson(input, PageDto.class);
			PageDto<ExtensionInfo> output = new PageDto<ExtensionInfo>();
			output.page = tmpPage.page;
			output.pageSize = tmpPage.pageSize;
			output.totalPages = tmpPage.totalPages;
			output.totalSize = tmpPage.totalSize;
			output.entries = Lists.newArrayList();
			for (Object obj : tmpPage.entries){
				ExtensionInfo info = gson.fromJson(gson.toJson(obj), ExtensionInfo.class);
				output.entries.add(info);
			}
			return output;
		}
	};
}
