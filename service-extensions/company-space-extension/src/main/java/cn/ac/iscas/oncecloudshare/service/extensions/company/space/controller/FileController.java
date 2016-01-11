package cn.ac.iscas.oncecloudshare.service.extensions.company.space.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.SpaceFileVersionDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.utils.CompanyUtils;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.utils.CompanyUtils.ErrorCodes;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

@Controller("companySpaceFileController")
@RequestMapping(value = "/api/v2/exts/company/space/files/{fileId:root|\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class FileController extends CompanySpaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(FileController.class);
	@Resource
	private FileContentService fcService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable String fileId) {
		initSpace(model);
		initFile(model, fileId);
	}

	/**
	 * 获取单个文件(夹)的元数据
	 * 
	 * @param file
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String get(@ModelAttribute("file") SpaceFile file) {
		return gson().toJson(fileToDto.apply(file));
	}

	@RequestMapping(value = "children", method = RequestMethod.GET)
	@ResponseBody
	public String getChildren(@ModelAttribute("file") SpaceFile file, @RequestParam(value = "dir", required = false) Boolean isDir,
			PageParam pageParam) {
		if (!file.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST, "对文件执行目录查询的操作");
		}
		Page<SpaceFile> children = spaceService.findChildren(file.getId(), isDir, pageParam.getPageable(SpaceFile.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(children, fileToDto));
	}

	/**
	 * 创建目录。只有管理员才有权限创建目录
	 * 
	 * @param space
	 * @param parent
	 * @param name
	 * @return
	 */
	@RequiresRoles({ "sys:admin" })
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String makeFolder(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile parent, String name) {
		SpaceFile folder = spaceService.makeFolder(currentUser(), space, parent, name);
		postEvent(new SpaceFileEvent(getUserPrincipal(), space, folder, SpaceFileEvent.EVENT_MAKE_FOLDER));
		return gson().toJson(fileToDto.apply(folder));
	}

	/**
	 * 移动文件(夹)。只有管理员才有权限移动
	 * 
	 * @param space
	 * @param toRemove
	 * @param parentId
	 * @param name
	 * @return
	 */
	@RequiresRoles({ "sys:admin" })
	@RequestMapping(params = "parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String move(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile toRemove, @RequestParam String parentId,
			@RequestParam String name) {
		SpaceFile newParent = "root".equals(parentId) ? spaceService.findRoot(space.getId()) : spaceService.findFolder(space,
				NumberUtils.toLong(parentId, -1));
		if (newParent == null) {
			throw new RestException(ErrorCodes.FILE_NOT_FOUND);
		}
		SpaceFileEvent event = new SpaceFileMoveEvent(getUserPrincipal(), space, toRemove);
		spaceService.move(toRemove, newParent, name);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 改名。需要管理员权限
	 * 
	 * @param space
	 * @param file
	 * @param name
	 * @return
	 */
	@RequiresRoles({ "sys:admin" })
	@RequestMapping(params = { "!parentId", "name" }, method = RequestMethod.PUT)
	@ResponseBody
	public String rename(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file, String name) {
		SpaceFileEvent event = new SpaceFileMoveEvent(getUserPrincipal(), space, file);
		spaceService.move(file, file.getParent(), name);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresRoles({ "sys:admin" })
	@RequestMapping(params = "!parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String updateInfo(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file, @RequestParam String description) {
		SpaceFileEvent event = new SpaceFileUpdateEvent(getUserPrincipal(), space, file);
		spaceService.updateInfo(file, description);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresRoles({ "sys:admin" })
	@RequestMapping(value = "trash", method = RequestMethod.PUT)
	@ResponseBody
	public String trash(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file) {
		spaceService.trash(file);
		postEvent(new SpaceFileEvent(getUserPrincipal(), space, file, SpaceFileEvent.EVENT_TRASH));
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresRoles({ "sys:admin" })
	@RequestMapping(value = "untrash", method = RequestMethod.PUT)
	@ResponseBody
	public String untrash(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file,
			@RequestParam(required = false) Long parentId, @RequestParam(required = false) String name) {
		SpaceFileEvent event = new SpaceFileUntrashEvent(getUserPrincipal(), space, file);
		if (parentId == null || name == null) {
			spaceService.untrash(file);
		} else {
			SpaceFile parent = spaceService.findFolder(space, parentId);
			if (parent == null) {
				throw new RestException(ErrorCodes.FILE_NOT_FOUND);
			}
			spaceService.untrashTo(file, parent, name);
		}
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresRoles({ "sys:admin" })
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file) {
		spaceService.delete(file);
		postEvent(new SpaceFileEvent(getUserPrincipal(), space, file, SpaceFileEvent.EVENT_DELETE));
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "follow", method = RequestMethod.PUT)
	@ResponseBody
	public String follow(@ModelAttribute("file") SpaceFile file) {
		SpaceFileFollow favorite = new SpaceFileFollow(currentUser(), file);
		try {
			spaceFileFollowService.save(favorite);
			return gson().toJson(ResponseDto.OK);
		} catch (DataIntegrityViolationException e) {
			throw new RestException(ErrorCode.CONFLICT);
		}
	}

	@RequestMapping(value = "unfollow", method = RequestMethod.PUT)
	@ResponseBody
	public String unfollow(@ModelAttribute("file") SpaceFile file) {
		spaceFileFollowService.delete(currentUserId(), file.getId());
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 上传新文件（multipart）
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String upload(HttpServletRequest request, @ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile folder,
			@RequestParam(required = false) String name, @RequestParam(value = "file") MultipartFile file) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		name = Objects.firstNonNull(name, file.getOriginalFilename());
		spaceService.checkFileExtenstion(name);
		SpaceFileVersion fv = null;
		if (SecurityUtils.getSubject().hasRole("sys:admin")) {
			fv = companySpaceService.uploadFileByAdmin(currentUser(), folder, name, new MultipartFileByteSource(file));
		} else {
			fv = companySpaceService.uploadFileByUser(currentUser(), folder, name, new MultipartFileByteSource(file));
		}
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 上传新文件（md5）
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String uploadByMd5(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile folder, @RequestParam String name,
			@RequestParam String md5) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		spaceService.checkMd5FileExists(md5);
		SpaceFileVersion fv = null;
		if (SecurityUtils.getSubject().hasRole("sys:admin")) {
			fv = companySpaceService.uploadFileByAdmin(currentUser(), folder, name, md5);
		} else {
			fv = companySpaceService.uploadFileByUser(currentUser(), folder, name, md5);
		}
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 上传临时文件片段（断点续传）
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = "type=fragment", headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadFileFragment(@ModelAttribute("space") BaseSpace space, @RequestParam MultipartFile fileFragment) throws IOException {
		SpaceFileVersion fv = spaceService.saveTempFileFragment(currentUser(), space, new MultipartFileByteSource(fileFragment));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 合并临时文件片段（断点续传）
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = "fragmentIds")
	@ResponseBody
	public String mergeFileFragments(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile folder, @RequestParam String name,
			@RequestParam long[] fragmentIds) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		spaceService.checkFileExtenstion(name);
		SpaceFileVersion fv = null;
		if (SecurityUtils.getSubject().hasRole("sys:admin")) {
			fv = companySpaceService.mergeTempFileFragmentByAdmin(currentUser(), folder, name, Longs.asList(fragmentIds));
		} else {
			fv = companySpaceService.mergeTempFileFragmentByUser(currentUser(), folder, name, Longs.asList(fragmentIds));
		}
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 下载最新版本
	 */
	@RequestMapping(value = "content", method = RequestMethod.GET)
	@ResponseBody
	public void downloadHeadVersion(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("space") BaseSpace space,
			@ModelAttribute("file") SpaceFile file) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
		}
		downloadFileVersion(file.getHeadVersion(), request, response);

		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, file.getHeadVersion(), SpaceFileVersionEvent.EVENT_DOWNLOAD));
	}

	@RequestMapping(value = "downloadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateHeadVersionDownloadTicket(@ModelAttribute("file") SpaceFile file) {
		if (file.getIsDir()) {
			throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
		}
		return gson().toJson(fcService.generateDownloadTicket(file.getHeadVersion()));
	}

	/**
	 * 获取所有版本信息
	 */
	@RequestMapping(value = "versions", method = RequestMethod.GET)
	@ResponseBody
	public String getAllVersions(@ModelAttribute("file") SpaceFile file) {
		if (file.getIsDir()) {
			throw new RestException(CompanyUtils.ErrorCodes.FILE_EXPECTED);
		}
		return gson().toJson(Lists.transform(file.getVersions(), SpaceFileVersionDto.defaultTransformer));
	}

	/**
	 * 获取单个版本信息
	 */
	@RequestMapping(value = "versions/{ver}", method = RequestMethod.GET)
	@ResponseBody
	public String getVersion(@ModelAttribute("file") SpaceFile file, @PathVariable Integer ver) {
		if (file.getIsDir()) {
			throw new RestException(CompanyUtils.ErrorCodes.FILE_EXPECTED);
		}
		SpaceFileVersion fv = file.getVersion(ver);
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}

	/**
	 * 上传新版本（multipart）
	 */
	@RequestMapping(value = "versions", method = RequestMethod.POST, params = "file", headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadNewVersion(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file,
			@RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(CompanyUtils.ErrorCodes.FILE_EXPECTED);
		}
		SpaceFileVersion fv = null;
		if (SecurityUtils.getSubject().hasRole("sys:admin")) {
			fv = spaceService.saveNewFileVersion(currentUser(), file, null, new MultipartFileByteSource(multipartFile));
		} else {
			fv = companySpaceService.uploadNewFileVersionByUser(currentUser(), file, new MultipartFileByteSource(multipartFile));
		}
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}

	/**
	 * 上传新版本（md5）
	 */
	@RequestMapping(value = "versions", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String uploadNewVersionByMd5(@ModelAttribute("space") BaseSpace space, @ModelAttribute("file") SpaceFile file, @RequestParam String md5)
			throws IOException {
		spaceService.checkMd5FileExists(md5);
		if (file.getIsDir()) {
			throw new RestException(CompanyUtils.ErrorCodes.FILE_EXPECTED);
		}
		SpaceFileVersion fv = null;
		if (SecurityUtils.getSubject().hasRole("sys:admin")) {
			fv = spaceService.saveNewFileVersion(currentUser(), file, md5, null);
		} else {
			fv = companySpaceService.uploadNewFileVersionByUser(currentUser(), file, md5);
		}
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}

	/**
	 * 下载文件版本
	 */
	@RequestMapping(value = "versions/{ver}/content", method = RequestMethod.GET)
	@ResponseBody
	public void downloadFileVersion(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("space") BaseSpace space,
			@ModelAttribute("file") SpaceFile file, @PathVariable Integer ver) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(CompanyUtils.ErrorCodes.FILE_EXPECTED);
		}
		SpaceFileVersion fv = file.getVersion(ver);
		if (fv == null) {
			throw new RestException(ErrorCode.FILE_VERSION_NOT_FOUND);
		}
		downloadFileVersion(fv, request, response);
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), space, fv, SpaceFileVersionEvent.EVENT_DOWNLOAD));
	}
}