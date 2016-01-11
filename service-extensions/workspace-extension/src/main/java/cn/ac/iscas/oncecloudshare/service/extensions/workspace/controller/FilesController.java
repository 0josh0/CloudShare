package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.DownloadTicketDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.FileService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Permissions;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.Lists;

@Controller("workspaceFilesController")
@RequestMapping(value = "/api/v2/exts/workspaces/{workspaceId:\\d+}/files", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class FilesController extends WorkspaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(FilesController.class);
	@Resource
	private SpaceService spaceService;
	@Resource
	private FileContentService fcService;
	
	@Resource(name = "workspaceFileService")
	private FileService fileService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long workspaceId) {
		initWorkspace(model, workspaceId);
	}

	/**
	 * 搜索文件
	 */
	@RequiresPermissions({ Permissions.WorkSpace.DOWNLOAD, Permissions.WorkSpace.FOLLOW })
	@RequestMapping(value = { "", "search" }, params = "follow", method = RequestMethod.GET)
	@ResponseBody
	public String follows(@ModelAttribute("workspace") Workspace workspace, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("file.owner.id", Operator.EQ, workspace.getSpace().getId()));
		filters.add(new SearchFilter("user.id", Operator.EQ, currentUserId()));
		if (Roles.SEPARATED.equals(currentRole())) {
			filters.add(new SearchFilter("file.creator.id", Operator.EQ, getUserPrincipal().getUserId()));
		}
		Page<SpaceFileFollow> page = spaceFileFollowService.findAll(filters, pageParam.getPageable(SpaceFileFollow.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(page, SpaceFileDto.followTransformer));
	}

	/**
	 * 搜索文件
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = { "", "search" }, params = "!follow", method = RequestMethod.GET)
	@ResponseBody
	public String search(@ModelAttribute("workspace") Workspace workspace, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("owner.id", Operator.EQ, workspace.getSpace().getId()));
		filters.add(new SearchFilter("modifiable", Operator.EQ, true));
		filters.add(new SearchFilter("status", Operator.NE, FileStatus.DELETED));
		if (Roles.SEPARATED.equals(currentRole())) {
			filters.add(new SearchFilter("creator.id", Operator.EQ, getUserPrincipal().getUserId()));
		}
		Page<SpaceFile> page = spaceService.findFiles(filters, pageParam.getPageable(SpaceFile.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(page, fileToDto));
	}
	
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "downloadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateBatchDownloadTicket(@ModelAttribute("workspace") Workspace workspace, @RequestParam long[] fileIds) throws IOException {
		int sizeLimit = fcService.getBatchDownloadNumberLimit();
		if (fileIds.length > sizeLimit) {
			throw new RestException(ErrorCode.BATCH_DOWNLOAD_EXCEED_LIMIT, "batch download number limit: " + sizeLimit);
		}
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner", Operator.EQ, workspace.getSpace()));
		filters.add(new SearchFilter("id", Operator.IN, fileIds));
		filters.add(new SearchFilter("status", Operator.EQ, FileStatus.HEALTHY));
		if (Roles.SEPARATED.equals(currentRole())) {
			filters.add(new SearchFilter("creator.id", Operator.EQ, getUserPrincipal().getUserId()));
		}
		List<SpaceFile> files = spaceService.findFiles(filters);
		if (files.size() != fileIds.length) {
			throw new RestException(ErrorCode.BAD_REQUEST, "invalid file id");
		}
		DownloadTicketDto dto = fcService.generateDownloadFilesTicket(files);
		
		for (SpaceFile file : files){
			fileService.incrDownloads(file, 1);
		}
		
		return gson().toJson(dto);
	}
}