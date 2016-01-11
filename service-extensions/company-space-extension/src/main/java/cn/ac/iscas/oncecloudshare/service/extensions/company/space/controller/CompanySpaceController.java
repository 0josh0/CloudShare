package cn.ac.iscas.oncecloudshare.service.extensions.company.space.controller;

import java.io.IOException;
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
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.DownloadTicketDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpace;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpaceApplication;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.Lists;

@Controller("companySpaceController")
@RequestMapping(value = "/api/v2/exts/company/space", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
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
	 * 查看我的申请记录
	 * 
	 * @param space
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "applications", method = RequestMethod.GET)
	@ResponseBody
	public String applications(@ModelAttribute("space") CompanySpace space, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("applicant.id", Operator.EQ, currentUserId()));
		Page<CompanySpaceApplication> page = companySpaceService.findApplications(filters, pageParam.getPageable(CompanySpaceApplication.class));
		return Gsons.filterByFields(ApplicationDto.class, pageParam.getFields()).toJson(PageDto.of(page, ApplicationDto.DEFAULT_TRANSFORMER));
	}

	/**
	 * 搜索文件
	 */
	@RequestMapping(value = { "files", "files/search" }, params = "follow", method = RequestMethod.GET)
	@ResponseBody
	public String follows(@ModelAttribute("space") CompanySpace space, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("file.owner.id", Operator.EQ, space.getId()));
		filters.add(new SearchFilter("user.id", Operator.EQ, currentUserId()));
		Page<SpaceFileFollow> page = spaceFileFollowService.findAll(filters, pageParam.getPageable(SpaceFileFollow.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(page, SpaceFileDto.followTransformer));
	}

	/**
	 * 搜索文件
	 */
	@RequestMapping(value = { "files", "files/search" }, params = "!follow", method = RequestMethod.GET)
	@ResponseBody
	public String search(@ModelAttribute("space") BaseSpace space, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("owner.id", Operator.EQ, space.getId()));
		filters.add(new SearchFilter("modifiable", Operator.EQ, true));
		filters.add(new SearchFilter("status", Operator.NE, FileStatus.DELETED));
		Page<SpaceFile> page = spaceService.findFiles(filters, pageParam.getPageable(SpaceFile.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(page, fileToDto));
	}
	
	@RequestMapping(value = "files/downloadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateBatchDownloadTicket(@ModelAttribute("space") BaseSpace space, @RequestParam long[] fileIds) throws IOException {
		int sizeLimit = fcService.getBatchDownloadNumberLimit();
		if (fileIds.length > sizeLimit) {
			throw new RestException(ErrorCode.BATCH_DOWNLOAD_EXCEED_LIMIT, "batch download number limit: " + sizeLimit);
		}
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner", Operator.EQ, space));
		filters.add(new SearchFilter("id", Operator.IN, fileIds));
		filters.add(new SearchFilter("status", Operator.EQ, FileStatus.HEALTHY));
		List<SpaceFile> files = spaceService.findFiles(filters);
		if (files.size() != fileIds.length) {
			throw new RestException(ErrorCode.BAD_REQUEST, "invalid file id");
		}
		DownloadTicketDto dto = fcService.generateDownloadFilesTicket(files);
		return gson().toJson(dto);
	}
}