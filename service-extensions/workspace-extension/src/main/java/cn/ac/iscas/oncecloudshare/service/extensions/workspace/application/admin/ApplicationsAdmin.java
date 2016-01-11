package cn.ac.iscas.oncecloudshare.service.extensions.workspace.application.admin;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.dto.AdminApplicationDto;
import cn.ac.iscas.oncecloudshare.service.application.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.application.model.AdminApplication;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

@Controller
@RequestMapping(value = "/adminapi/{ver}/applys", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ApplicationsAdmin extends BaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(ApplicationsAdmin.class);

	@Resource
	private ApplicationService applicationService;

	/**
	 * 查询当前有哪些工作空间申请
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(params = { "domain=admin" }, method = RequestMethod.GET)
	@ResponseBody
	public String listAdminApplications(@RequestParam(required = false) String q, PageParam pageParam) {
		// 判断用户是否是管理员
		if (!currentUser().hasRole("sys", "admin")) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		List<SearchFilter> filters = decodeFilters(q);
		Page<AdminApplication> page = applicationService.findApplications(AdminApplication.class, filters,
				pageParam.getPageable(AdminApplication.class));
		return Gsons.filterByFields(ApplicationDto.class, pageParam.getFields()).toJson(PageDto.of(page, AdminApplicationDto.DEFAULT_TRANSFORMER));
	}
}