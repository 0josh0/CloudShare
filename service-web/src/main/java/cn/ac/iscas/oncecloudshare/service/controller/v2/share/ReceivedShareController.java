package cn.ac.iscas.oncecloudshare.service.controller.v2.share;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.dto.share.ReceivedShareDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FolderService;
import cn.ac.iscas.oncecloudshare.service.service.share.ReceivedShareService;
import cn.ac.iscas.oncecloudshare.service.service.share.ShareService2;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.Lists;

/**
 * 提供我收到的分享的相关功能，包括：
 * 
 * <pre>
 * 删除我收到的分享    DELETE    /api/v2/shares/received/{id}
 * 获取收到的分享文件夹的内容    GET    /api/v2/shares/received/{id}/children
 * 下载我收到的分享    GET    /api/v2/shares/received/{id}/content
 * </pre>
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Controller
@RequestMapping(value = "/api/v2/shares/received/{shareId:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ReceivedShareController extends BaseController {
	@Resource
	private ShareService2 shareService2;
	@Resource
	private FileService fileService;
	@Resource
	private FolderService folderService;
	@Resource
	private ReceivedShareService receivedShareService;
	@Resource
	private FileContentService fileContentService;

	@InitBinder({ "receivedShare" })
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("abcdefg");
	}

	@ModelAttribute
	public void initModel(Model model, @PathVariable long shareId) {
		// 初始化share
		ReceivedShare share = receivedShareService.findOne(shareId);
		if (share == null || share.getIsDeleted() || share.getShare().getStatus().equals(Share.Status.CANCELED)) {
			throw new RestException(ErrorCode.USERSHARE_NOT_FOUND);
		}
		if (FileStatus.DELETED.equals(share.getShare().getFile().getStatus())){
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		if (!share.getRecipient().getId().equals(currentUserId())) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		model.addAttribute("receivedShare", share);
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String get(@ModelAttribute("receivedShare") ReceivedShare share) {
		return gson().toJson(ReceivedShareDto.toBrief.apply(share));
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@ModelAttribute("receivedShare") ReceivedShare share) {
		receivedShareService.delete(share);
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "content", method = RequestMethod.GET)
	public void content(@ModelAttribute("receivedShare") ReceivedShare share, @RequestParam(value = "fileId", required = false) Long fileId,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		File file = initFolderOrFile(share, fileId);
		if (file.getIsDir()) {
			throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
		} else {
			if (file.equals(share.getShare().getFile())) {
				downloadFileVersion(share.getShare().getSharedFileVersion(), request, response);
			} else {
				downloadFileVersion(file.getHeadVersion(), request, response);
			}
		}
	}

	@RequestMapping(value = "children", method = RequestMethod.GET)
	@ResponseBody
	public String getChildren(@ModelAttribute("receivedShare") ReceivedShare share,
			@RequestParam(value = "folderId", required = false) Long folderId, PageParam pageParam) {
		File folder = initFolder(share, folderId);
		Page<File> chilren = folderService.findChildren(folder.getId(), pageParam.getPageable(File.class));
		return Gsons.filterByFields(FileDto.class, pageParam.getFields()).toJson(PageDto.of(chilren, FileDto.TRANSFORMER));
	}

	/**
	 * 查询收到的指定分享的文件
	 * 
	 * @param share
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "findFiles", method = RequestMethod.GET)
	@ResponseBody
	public String findFiles(@ModelAttribute("receivedShare") ReceivedShare share, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> and = decodeFilters(q);
		List<SearchFilter> or = Lists.newArrayList();
		File file = share.getShare().getFile();
		if (file.getIsDir()) {
			or.add(new SearchFilter("id", Operator.EQ, file.getId()));
			or.add(new SearchFilter("path", Operator.LIKE, file.getPath() + "/%"));
		} else {
			and.add(new SearchFilter("id", Operator.EQ, file.getId()));
		}
		and.add(new SearchFilter("status", Operator.EQ, FileStatus.HEALTHY));
		Page<File> files = folderService.findAll(and, or, pageParam.getPageable(File.class));
		return Gsons.filterByFields(FileDto.class, pageParam.getFields()).toJson(PageDto.of(files, FileDto.TRANSFORMER));
	}

	/**
	 * 产生下载文件版本的ticket
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "downloadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateDownloadTicket(@ModelAttribute("receivedShare") ReceivedShare share,
			@RequestParam(value = "fileId", required = false) Long[] fileIds) throws IOException {
		File sharedFile = share.getShare().getFile();
		// 如果分享的是文件夹
		if (sharedFile.getIsDir()) {
			if (fileIds == null || fileIds.length == 0) {
				throw new RestException(ErrorCode.BAD_REQUEST);
			}
			int sizeLimit = fileContentService.getBatchDownloadNumberLimit();
			if (fileIds.length > sizeLimit) {
				throw new RestException(ErrorCode.BATCH_DOWNLOAD_EXCEED_LIMIT, "batch download number limit: " + sizeLimit);
			}
			List<SearchFilter> filters = Lists.newArrayList();
			filters.add(new SearchFilter("path", Operator.LIKE, sharedFile.getPath() + "/%"));
			filters.add(new SearchFilter("id", Operator.IN, fileIds));
			filters.add(new SearchFilter("status", Operator.EQ, FileStatus.HEALTHY));
			filters.add(new SearchFilter("isDir", Operator.EQ, Boolean.FALSE));
			List<File> files = fileService.findAll(filters);
			if (files.size() != fileIds.length) {
				throw new RestException(ErrorCode.BAD_REQUEST, "invalid file id");
			}
			List<FileVersion> versions = Lists.newArrayList();
			for (File file : files) {
				versions.add(file.getHeadVersion());
			}
			return gson.toJson(fileContentService.generateDownloadTicket(versions));
		}
		// 如果分享的是文件
		else {
			return gson().toJson(fileContentService.generateDownloadTicket(share.getShare().getSharedFileVersion()));
		}
	}

	protected File initFile(ReceivedShare share, Long fileId) {
		File file = initFolderOrFile(share, fileId);
		if (file.getIsDir()) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		return file;
	}

	protected File initFolder(ReceivedShare share, Long folderId) {
		File folder = initFolderOrFile(share, folderId);
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		return folder;
	}

	protected File initFolderOrFile(ReceivedShare share, Long fileId) {
		File sharedFile = share.getShare().getFile();
		if (sharedFile.getStatus().equals(FileStatus.DELETED)){
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		if (fileId == null || sharedFile.getId().equals(fileId)) {
			return sharedFile;
		}
		if (!sharedFile.getIsDir()) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		File file = fileService.find(fileId);
		if (file == null) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		if (!file.isChildOf(sharedFile)) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		return file;
	}
}