package cn.ac.iscas.oncecloudshare.service.controller.v2.common;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager;
import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager.ExtensionHolder;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/v2/extensions", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ExtensionController extends BaseController {
	@Resource
	ExtensionManager extManager;

	protected ExtensionHolder findExtension(String extName) {
		ExtensionHolder holder = extManager.find(extName);
		if (holder == null) {
			throw new RestException(ErrorCode.EXT_NOT_FOUND);
		}
		return holder;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String getAll() {
		return gson().toJson(extManager.findAll());
	}

	@RequestMapping(value = "{extName}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String extName) {
		return gson().toJson(findExtension(extName));
	}
}
