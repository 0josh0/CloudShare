package cn.ac.iscas.oncecloudshare.exts.api.web;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.exts.model.Extension;
import cn.ac.iscas.oncecloudshare.exts.service.ExtensionServcie;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;

@Controller
@RequestMapping(value = "/api/extensions/{id:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ExtensionApi extends BaseController {
	@Resource
	private ExtensionServcie extensionServcie;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long id) {
		Extension extension = extensionServcie.findOne(id);
		if (extension == null) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		model.addAttribute("extension", extension);
	}

	@RequestMapping(params = "download", method = RequestMethod.GET)
	@ResponseBody
	public void download(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("extension") Extension extension)
			throws IOException {
		File file = new File(extension.getFilePath());
		ByteSource source = Files.asByteSource(file);
		initDownload(request, response, source, file.getName());
	}
}
