package cn.ac.iscas.oncecloudshare.monitor.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.monitor.model.Server;
import cn.ac.iscas.oncecloudshare.monitor.service.ServerMonitor;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/servers", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ServersController extends BaseController {
	@Resource
	private ServerMonitor serverMonitor;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String findAll(PageParam pageParam) {
		return gson().toJson(serverMonitor.findAll(pageParam.getPageable(Server.class)));
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String create(Server server) {
		return gson().toJson(serverMonitor.create(server));
	}
}
