package cn.ac.iscas.oncecloudshare.exts.api;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.exts.api.dto.ExtensionDto;
import cn.ac.iscas.oncecloudshare.exts.model.Extension;

import com.google.common.base.Function;

@Component
public class ExtensionTransformers {
	public Function<Extension, ExtensionDto> extensionToDto = new Function<Extension, ExtensionDto>() {
		public ExtensionDto apply(Extension input) {
			if (input == null) {
				return null;
			}
			ExtensionDto output = new ExtensionDto();
			output.id = input.getId();
			output.name = input.getName();
			output.version = input.getVersion();
			output.description = input.getDescription();
			output.minSupport = input.getMinSupport();
			output.maxSupport = input.getMaxSupport();
			if (input.getCreateTime() != null) {
				output.createTime = input.getCreateTime().getTime();
			}
			return output;
		}
	};
}
