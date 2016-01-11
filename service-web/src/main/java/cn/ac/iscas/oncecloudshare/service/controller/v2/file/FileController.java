package cn.ac.iscas.oncecloudshare.service.controller.v2.file;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.ac.iscas.oncecloudshare.service.controller.v2.MultipartFileByteSource;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.DownloadTicketDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileVersionDto;
import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileRenameEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

@Controller
@RequestMapping(value = "/api/v2/files", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class FileController extends BaseFileController {

	@Autowired
	protected FileContentService fcService;

	/**
	 * 搜索文件
	 */
	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q, PageParam pageParam) {
		Page<File> page = fileService.search(currentUserId(), q, pageParam.getPageable(File.class));
		return Gsons.filterByFields(FileDto.class, pageParam.getFields()).toJson(PageDto.of(page, FileDto.TRANSFORMER));
	}

	@RequestMapping(value = "downloadTickets", params = { "fileVersionIdList", "!fileIds" }, method = RequestMethod.POST)
	@ResponseBody
	public String generateDownloadTicket(@RequestParam long[] fileVersionIdList) throws IOException {
		int sizeLimit = fcService.getBatchDownloadNumberLimit();
		if (fileVersionIdList.length > sizeLimit) {
			throw new RestException(ErrorCode.BATCH_DOWNLOAD_EXCEED_LIMIT, "batch download number limit: " + sizeLimit);
		}
		List<FileVersion> fvList = fileService.findFileVersionsByOwner(currentUserId(), Longs.asList(fileVersionIdList));
		if (fvList.size() != fileVersionIdList.length) {
			throw new RestException(ErrorCode.BAD_REQUEST, "invalid file version id");
		}
		DownloadTicketDto dto = fcService.generateDownloadTicket(fvList);
		return gson().toJson(dto);
	}

	@RequestMapping(value = "downloadTickets", params = "fileIds", method = RequestMethod.POST)
	@ResponseBody
	public String generateDownloadFilesTicket(@RequestParam long[] fileIds) throws IOException {
		int sizeLimit = fcService.getBatchDownloadNumberLimit();
		if (fileIds.length > sizeLimit) {
			throw new RestException(ErrorCode.BATCH_DOWNLOAD_EXCEED_LIMIT, "batch download number limit: " + sizeLimit);
		}
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, currentUserId()));
		filters.add(new SearchFilter("modifiable", Operator.EQ, true));
		filters.add(new SearchFilter("id", Operator.IN, fileIds));
		filters.add(new SearchFilter("status", Operator.EQ, FileStatus.HEALTHY));
		List<File> files = fileService.findAll(filters);
		if (files.size() != fileIds.length) {
			throw new RestException(ErrorCode.BAD_REQUEST, "invalid file id");
		}
		DownloadTicketDto dto = fcService.generateDownloadFilesTicket(files);
		return gson().toJson(dto);
	}

	/**
	 * 获取单个文件元数据
	 */
	@RequestMapping(value = "{fileId:\\d+}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable Long fileId) {
		File file = findFile(fileId);
		return gson().toJson(FileDto.of(file));
	}

	/**
	 * 上传新文件（multipart）
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String upload(HttpServletRequest request, @RequestParam Long parentId, @RequestParam(required = false) String name,
			@RequestParam(value = "file") MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		findParent(parentId);
		name = Objects.firstNonNull(name, file.getOriginalFilename());
		checkFileExtenstion(name);
		FileVersion fv = fileService.saveNewFile(currentUserId(), parentId, name, null, new MultipartFileByteSource(file));
		// 发送文件上传的时间

		FileVersionEvent event = new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_UPLOAD);
		
		postEvent(event);

		return gson().toJson(FileDto.of(fv.getFile()));
	}

	/**
	 * 上传新文件（md5）
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String uploadByMd5(@RequestParam Long parentId, @RequestParam String name, @RequestParam String md5) throws IOException {
		checkMd5FileExists(md5);
		findParent(parentId);
		FileVersion fv = fileService.saveNewFile(currentUserId(), parentId, name, md5, null);
		// 发送文件上传的时间
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileDto.of(fv.getFile()));
	}

	/**
	 * 产生上传新文件的ticket
	 */
	@RequestMapping(value = "/uploadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateFileUploadTicket(@RequestParam Long parentId) throws IOException {
		File parent = findParent(parentId);
		return gson().toJson(fcService.generateFileUploadTicket(parent));
	}

	/**
	 * 上传临时文件片段（断点续传）
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, params = "type=fragment", headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadFileFragment(@RequestParam MultipartFile fileFragment) throws IOException {
		if (fileFragment.isEmpty()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		FileVersion fv = fileService.saveTempFileFragment(currentUserId(), new MultipartFileByteSource(fileFragment));
		return gson().toJson(FileDto.of(fv.getFile()));
	}

	/**
	 * 合并临时文件片段（断点续传）
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, params = "fragmentIds")
	@ResponseBody
	public String mergeTempFragmentsAsNewFile(@RequestParam Long parentId, @RequestParam String name, @RequestParam long[] fragmentIds)
			throws IOException {
		findParent(parentId);
		checkFileExtenstion(name);
		FileVersion fv = fileService.mergeTempFragmentsAsNewFile(currentUserId(), Longs.asList(fragmentIds), parentId, name);
		// 发送文件上传的事件
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileDto.of(fv.getFile()));
	}

	/**
	 * 下载最新版本
	 */
	@RequestMapping(value = "{fileId:\\d+}/content", method = RequestMethod.GET)
	@ResponseBody
	public void downloadHeadVersion(HttpServletRequest request, HttpServletResponse response, @PathVariable Long fileId) throws IOException {
		File file = findFile(fileId);
		downloadFileVersion(file.getHeadVersion(), request, response);

		// 发送文件上传的事件
		postEvent(new FileVersionEvent(getUserPrincipal(), file.getHeadVersion(), FileVersionEvent.EVENT_DOWNLOAD));
	}

	// @RequestMapping(value="{fileId:\\d+}/downloadTickets",method=RequestMethod.POST)
	// @ResponseBody
	// public String generateHeadVersionDownloadTicket(@PathVariable Long
	// fileId){
	// File file=findFile(fileId);
	// return
	// gson().toJson(fcService.generateDownloadTicket(file.getHeadVersion()));
	// }

	/**
	 * 获取所有版本信息
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions", method = RequestMethod.GET)
	@ResponseBody
	public String getAllVersions(@PathVariable Long fileId) {
		File file = findFile(fileId);
		return gson().toJson(Lists.transform(file.getVersions(), FileVersionDto.TRANSFORMER));
	}

	/**
	 * 获取单个版本信息
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions/{ver}", method = RequestMethod.GET)
	@ResponseBody
	public String getVersion(@PathVariable Long fileId, @PathVariable Integer ver) {
		FileVersion fv = findFileVersion(fileId, ver);
		return gson().toJson(FileVersionDto.of(fv));
	}

	/**
	 * 上传新版本（multipart）
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadNewVersion(@PathVariable Long fileId, @RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
		if (multipartFile.isEmpty()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		File file = findFile(fileId);
		FileVersion fv = fileService.saveNewFileVersion(file.getId(), null, new MultipartFileByteSource(multipartFile));
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileVersionDto.of(fv));
	}

	/**
	 * 上传新版本（md5）
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String uploadNewVersionByMd5(@PathVariable Long fileId, @RequestParam String md5) throws IOException {
		checkMd5FileExists(md5);
		File file = findFile(fileId);
		FileVersion fv = fileService.saveNewFileVersion(file.getId(), md5, null);
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileVersionDto.of(fv));
	}

	/**
	 * 合并临时文件片段，使之成为新版本（断点续传）
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions", method = RequestMethod.POST, params = "fragmentIds")
	@ResponseBody
	public String mergeTempFragmentsAsNewFileVersion(@PathVariable Long fileId, @RequestParam long[] fragmentIds) throws IOException {
		File file = findFile(fileId);
		FileVersion fv = fileService.mergeTempFragmentsAsNewFileVersion(currentUserId(), Longs.asList(fragmentIds), file.getId());
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileVersionDto.of(fv));
	}

	/**
	 * 产生上传新版本的ticket
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions/uploadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateFileVersionUploadTicket(@PathVariable Long fileId) throws IOException {
		File file = findFile(fileId);
		return gson().toJson(fcService.generateFileVersionUploadTicket(file));
	}

	/**
	 * 下载文件版本
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions/{ver}/content", method = RequestMethod.GET)
	@ResponseBody
	public void downloadFileVersion(HttpServletRequest request, HttpServletResponse response, @PathVariable Long fileId,
			@PathVariable Integer ver) throws IOException {
		FileVersion fv = findFileVersion(fileId, ver);
		downloadFileVersion(fv, request, response);
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_DOWNLOAD));
	}

	// /**
	// * 产生下载文件版本的ticket
	// */
	// @RequestMapping(value="{fileId:\\d+}/versions/{ver}/downloadTickets",method=RequestMethod.POST)
	// @ResponseBody
	// public String generateFileVersionDownloadTicket(@PathVariable Long
	// fileId,
	// @PathVariable Integer ver){
	// FileVersion fv=findFileVersion(fileId,ver);
	// return gson().toJson(fcService.generateDownloadTicket(fv));
	// }

	/**
	 * 移动
	 */
	@RequestMapping(value = "{fileId:\\d+}", params = "parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String move(@PathVariable Long fileId, @RequestParam Long parentId, @RequestParam String name) {
		File file = findFile(fileId);
		@SuppressWarnings("unused")
		File parent = findParent(parentId);

		FileEvent event = null;
		if (file.getParent().getId().equals(parentId)) {
			event = new FileRenameEvent(getUserPrincipal(), file);
		} else {
			event = new FileMoveEvent(getUserPrincipal(), file);
		}
		fileService.moveFile(fileId, parentId, name);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 更新元数据
	 */
	@RequestMapping(value = "{fileId:\\d+}", params = "!parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String updateInfo(@PathVariable Long fileId, @RequestParam(required = false) String description,
			@RequestParam(required = false) Boolean favorite) {
		File file = findFile(fileId);
		FileUpdateEvent event = new FileUpdateEvent(getUserPrincipal(), file);
		fileService.updateInfo(fileId, description, favorite);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 添加标签
	 * 
	 * @param fileId
	 * @param tags
	 * @return
	 */
	@RequestMapping(value = "{fileId:\\d+}/tags", method = RequestMethod.POST)
	@ResponseBody
	public String addTags(@PathVariable long fileId, long[] tags) {
		File file = findFile(fileId);
		fileService.addTags(file, tags);
		return ok();
	}

	/**
	 * 删除标签
	 * 
	 * @param fileId
	 * @param tags
	 * @return
	 */
	@RequestMapping(value = "{fileId:\\d+}/tags", method = RequestMethod.DELETE)
	@ResponseBody
	public String removeTags(@PathVariable long fileId, long[] tags) {
		File file = findFile(fileId);
		fileService.removeTags(file, tags);
		return ok();
	}

	/**
	 * 移入回收站
	 */
	@RequestMapping(value = "{fileId:\\d+}/trash", method = RequestMethod.PUT)
	@ResponseBody
	public String trash(@PathVariable Long fileId) {
		File file = findFile(fileId);
		fileService.trash(fileId);
		postEvent(new FileEvent(getUserPrincipal(), file, FileEvent.EVENT_TRASH));
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 从回收站还原
	 */
	@RequestMapping(value = "{fileId:\\d+}/untrash", method = RequestMethod.PUT)
	@ResponseBody
	public String untrash(@PathVariable Long fileId, @RequestParam(required = false) Long parentId,
			@RequestParam(required = false) String name) {
		File file = findFile(fileId);
		FileUntrashEvent event = new FileUntrashEvent(getUserPrincipal(), file);
		if (parentId == null || name == null) {
			fileService.untrash(fileId);
		} else {
			@SuppressWarnings("unused")
			File parent = findParent(parentId);
			fileService.untrashTo(fileId, parentId, name);
		}
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 清空回收站
	 */
	@RequestMapping(value = "trash", method = RequestMethod.DELETE)
	@ResponseBody
	public String clearTrashs() {
		folderService.clearTrash(currentUserId());
		postEvent(new FileEvent(getUserPrincipal(), null, FileEvent.EVENT_CLEAR_TRASH));
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 删除
	 */
	@RequestMapping(value = "{fileId:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long fileId) {
		File file = findFile(fileId);
		fileService.delete(fileId);
		postEvent(new FileEvent(getUserPrincipal(), file, FileEvent.EVENT_DELETE));
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 删除版本
	 */
	@RequestMapping(value = "{fileId:\\d+}/versions/{ver}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteVersion(@PathVariable Long fileId, @PathVariable Integer ver) {
		FileVersion fv = findFileVersion(fileId, ver);
		fileService.deleteVersion(fileId, ver);
		postEvent(new FileVersionEvent(getUserPrincipal(), fv, FileVersionEvent.EVENT_DELETE));
		return gson().toJson(ResponseDto.OK);
	}
}
