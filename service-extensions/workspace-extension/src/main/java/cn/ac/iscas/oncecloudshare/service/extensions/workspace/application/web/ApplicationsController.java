package cn.ac.iscas.oncecloudshare.service.extensions.workspace.application.web;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/{ver}/applications", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ApplicationsController extends BaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(ApplicationsController.class);

	@Resource
	private ApplicationService applicationService;
}