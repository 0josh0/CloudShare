package cn.ac.iscas.oncecloudshare.service.controller.v2.system;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.system.ServerInfo;

import com.google.gson.JsonObject;

@Controller
@RequestMapping(value = "/api/rest/system/")
public class SystemController extends BaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(SystemController.class);

	@RequestMapping(value = { "service/info" }, method = RequestMethod.GET)
	@ResponseBody
	public String getServiceInfo(final HttpServletRequest request) {
		JsonObject ret = new JsonObject();
		ret.addProperty("versionNumber", ServerInfo.getVersionNumber());
		ret.addProperty("versionName", ServerInfo.getVersionName());
		return ret.toString();
	}
}
