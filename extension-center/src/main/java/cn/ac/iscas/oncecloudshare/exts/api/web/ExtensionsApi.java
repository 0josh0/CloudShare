package cn.ac.iscas.oncecloudshare.exts.api.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.exts.api.ExtensionTransformers;
import cn.ac.iscas.oncecloudshare.exts.api.dto.ExtensionDto;
import cn.ac.iscas.oncecloudshare.exts.api.dto.ExtensionUploadRequestDto;
import cn.ac.iscas.oncecloudshare.exts.model.Extension;
import cn.ac.iscas.oncecloudshare.exts.service.ExtensionServcie;
import cn.ac.iscas.oncecloudshare.guava.io.MultiPartFiles;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

import com.google.common.io.Files;

@Controller
@RequestMapping(value = "/api/extensions", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ExtensionsApi extends BaseController {
	@Resource
	private ExtensionServcie extensionServcie;
	@Resource
	private ExtensionTransformers extensionTransformers;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String findAll(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		Page<Extension> extensions = extensionServcie.findAll(filters, pageParam.getPageable(Extension.class));
		return Gsons.filterByFields(ExtensionDto.class, pageParam.getFields()).toJson(PageDto.of(extensions, extensionTransformers.extensionToDto));
	}

	@RequestMapping(method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String upload(ExtensionUploadRequestDto request) {
		Extension extension = extensionServcie.findOne(request.getName(), request.getVersion());
		if (extension != null) {
			throw new RestException(ErrorCode.CONFLICT);
		}
		// 保存文件
		File file = new File(extensionServcie.getSaveDir(), request.getName() + "-" + request.getVersion() + ".zip");
		try {
			Files.copy(MultiPartFiles.newInputStreamSupplier(request.getFile()), file);
		} catch (IOException e) {
			throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		extension = new Extension();
		extension.setName(request.getName());
		extension.setVersion(request.getVersion());
		extension.setDescription(request.getDescription());
		extension.setMinSupport(request.getMinSupport());
		extension.setMaxSupport(request.getMaxSupport());
		extension.setFilePath(file.getAbsolutePath());
		extension = extensionServcie.save(extension);
		return gson().toJson(extensionTransformers.extensionToDto.apply(extension));
	}
}
