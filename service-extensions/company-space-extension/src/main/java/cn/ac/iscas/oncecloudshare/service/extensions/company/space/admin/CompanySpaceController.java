package cn.ac.iscas.oncecloudshare.service.extensions.company.space.admin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.controller.CompanySpaceBaseController;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpaceApplication;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

@Controller("adminCompanySpaceController")
@RequestMapping(value = "/adminapi/v2/exts/company/space", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class CompanySpaceController extends CompanySpaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(CompanySpaceController.class);
	@Resource
	private FileContentService fcService;

	@ModelAttribute
	public void initModel(Model model) {
		initSpace(model);
	}

	/**
	 * 查询资源中心的申请
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "/applications", method = RequestMethod.GET)
	@ResponseBody
	public String listApplications(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		Page<CompanySpaceApplication> page = companySpaceService.findApplications(filters, pageParam.getPageable(CompanySpaceApplication.class));
		return Gsons.filterByFields(ApplicationDto.class, pageParam.getFields()).toJson(PageDto.of(page, ApplicationDto.DEFAULT_TRANSFORMER));
	}
}