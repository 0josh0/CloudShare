package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import org.springframework.web.multipart.MultipartFile;

import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.MultipartFileByteSource;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.NotifType;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.DownloadTicketDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileCommentCreateDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.FileCommentDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileVersionDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.TempFile;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.UploadFile;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.UploadFileVersion;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.service.FileService;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Configs;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Permissions;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils.ErrorCodes;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileComment;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.service.common.TempFileStorageService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileContentService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.guava.Functions;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.primitives.Longs;

@Controller("workspaceFileController")
@RequestMapping(value = "/api/v2/exts/workspaces/{workspaceId:\\d+}/files/{fileId:root|\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8,
		MediaTypes.JSON_UTF8 })
public class FileController extends WorkspaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(FileController.class);
	@Resource
	private SpaceService spaceService;
	@Resource
	private FileContentService fcService;
	@Resource
	private TempFileStorageService tfsService;
	@Resource
	private ApplicationService applicationService;

	@Resource(name = "workspaceFileService")
	private FileService fileService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long workspaceId, @PathVariable String fileId) {
		initWorkspace(model, workspaceId);
		initFile(model, fileId);
	}

	/**
	 * 获取单个文件(夹)的元数据
	 * 
	 * @param file
	 * @return
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String get(@ModelAttribute("file") SpaceFile file) {
		checkAccessible(file);
		return gson().toJson(fileToDto.apply(file));
	}

	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "children", method = RequestMethod.GET)
	@ResponseBody
	public String getChildren(@ModelAttribute("file") SpaceFile file, @RequestParam(value = "dir", required = false) Boolean isDir,
			PageParam pageParam) {
		if (!file.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST, "对文件执行目录查询的操作");
		}
		checkAccessible(file);
		
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("parent.id", Operator.EQ, file.getId()));
		filters.add(new SearchFilter("status", Operator.EQ, "HEALTHY"));
		filters.add(new SearchFilter("modifiable", Operator.EQ, Boolean.TRUE));
		if (isDir != null) {
			filters.add(new SearchFilter("isDir", Operator.EQ, isDir));
		}
		if (Roles.SEPARATED.equals(currentRole())) {
			filters.add(new SearchFilter("creator.id", Operator.EQ, getUserPrincipal().getUserId()));
		}
		Page<SpaceFile> children = spaceService.findFiles(filters, pageParam.getPageable(SpaceFile.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(children, fileToDto));
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String makeFolder(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile parent, String name) {
		checkAccessible(parent);
		
		SpaceFile folder = spaceService.makeFolder(currentUser(), workspace.getSpace(), parent, name);
		postEvent(new SpaceFileEvent(getUserPrincipal(), workspace, folder, SpaceFileEvent.EVENT_MAKE_FOLDER));
		return gson().toJson(fileToDto.apply(folder));
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(params = "parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String move(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile toRemove, @RequestParam String parentId,
			@RequestParam String name) {
		SpaceFile newParent = "root".equals(parentId) ? spaceService.findRoot(workspace.getSpace().getId()) : spaceService.findFolder(
				workspace.getSpace(), NumberUtils.toLong(parentId, -1));
		checkEditable(toRemove);
		checkAccessible(newParent);
		
		SpaceFileEvent event = new SpaceFileMoveEvent(getUserPrincipal(), workspace, toRemove);
		spaceService.move(toRemove, newParent, name);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(params = { "!parentId", "name" }, method = RequestMethod.PUT)
	@ResponseBody
	public String rename(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file, String name) {		
		checkEditable(file);		
		SpaceFileEvent event = new SpaceFileMoveEvent(getUserPrincipal(), workspace, file);
		spaceService.move(file, file.getParent(), name);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(params = "!parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String updateInfo(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file,
			@RequestParam(required = false) String description) {
		checkEditable(file);
		SpaceFileEvent event = new SpaceFileUpdateEvent(getUserPrincipal(), workspace, file);
		spaceService.updateInfo(file, description);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(value = "trash", method = RequestMethod.PUT)
	@ResponseBody
	public String trash(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file) {
		checkEditable(file);
		spaceService.trash(file);
		postEvent(new SpaceFileEvent(getUserPrincipal(), workspace, file, SpaceFileEvent.EVENT_TRASH));
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions(Permissions.WorkSpace.FOLLOW)
	@RequestMapping(value = "follow", method = RequestMethod.PUT)
	@ResponseBody
	public String follow(@ModelAttribute("file") SpaceFile file) {
		checkAccessible(file);
		spaceFileFollowService.follow(currentUser(), file);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions(Permissions.WorkSpace.FOLLOW)
	@RequestMapping(value = "unfollow", method = RequestMethod.PUT)
	@ResponseBody
	public String unfollow(@ModelAttribute("file") SpaceFile file) {
		checkAccessible(file);
		spaceFileFollowService.unfollow(currentUser(), file);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(value = "untrash", method = RequestMethod.PUT)
	@ResponseBody
	public String untrash(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file,
			@RequestParam(required = false) Long parentId, @RequestParam(required = false) String name) {
		checkEditable(file);
		SpaceFileEvent event = new SpaceFileUntrashEvent(getUserPrincipal(), workspace, file);
		if (parentId == null || name == null) {
			spaceService.untrash(file);
		} else {
			SpaceFile parent = spaceService.findFolder(workspace.getSpace(), parentId);
			checkAccessible(parent);
			spaceService.untrashTo(file, parent, name);
		}
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequiresPermissions("workspace:edit")
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file) {
		checkDeletable(file);
		spaceService.delete(file);
		postEvent(new SpaceFileEvent(getUserPrincipal(), workspace, file, SpaceFileEvent.EVENT_DELETE));
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 上传新文件（multipart）
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "/upload", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String upload(HttpServletRequest request, @ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile folder,
			@RequestParam(required = false) String name, @RequestParam(value = "file") MultipartFile file) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCodes.FOLDER_EXPECTED);
		}
		checkAccessible(folder);
		name = Objects.firstNonNull(name, file.getOriginalFilename());
		spaceService.checkFileExtenstion(name);
		SpaceFileVersion fv = spaceService.saveNewFile(currentUser(), folder, name, null, new MultipartFileByteSource(file));
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 上传新文件（md5）
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String uploadByMd5(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile folder, @RequestParam String name,
			@RequestParam String md5) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		checkAccessible(folder);
		spaceService.checkMd5FileExists(md5);
		SpaceFileVersion fv = spaceService.saveNewFile(currentUser(), folder, name, md5, null);
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 上传临时文件片段（断点续传）
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = "type=fragment", headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadFileFragment(@ModelAttribute("workspace") Workspace workspace, @RequestParam MultipartFile fileFragment) throws IOException {
		SpaceFileVersion fv = spaceService.saveTempFileFragment(currentUser(), workspace.getSpace(), new MultipartFileByteSource(fileFragment));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 合并临时文件片段（断点续传）
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = "fragmentIds")
	@ResponseBody
	public String mergeFileFragments(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile folder,
			@RequestParam String name, @RequestParam long[] fragmentIds) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		checkAccessible(folder);
		spaceService.checkFileExtenstion(name);
		SpaceFileVersion fv = spaceService.mergeTempFileFragment(currentUser(), folder, name, Longs.asList(fragmentIds));
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(fileToDto.apply(fv.getFile()));
	}

	/**
	 * 受限上传
	 * 
	 * @param workspace
	 * @param folder
	 * @param name
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@RequiresPermissions("workspace:limitedUpload")
	@RequestMapping(value = "/limitedUpload", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String limitedUpload(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile folder,
			@RequestParam(required = false) String name, @RequestParam(value = "file") MultipartFile file) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		name = Objects.firstNonNull(name, file.getOriginalFilename());
		spaceService.checkFileExtenstion(name);
		long expireIn = globalConfigService.getConfigAsInteger(Configs.Keys.APPLY_UPLOAD_EXPIRES, Configs.Defaults.APPLY_UPLOAD_EXPIRES)
				* DateUtils.MILLIS_PER_DAY;
		long expireAt = System.currentTimeMillis() + expireIn;
		String tfKey = tfsService.saveTempFile(new MultipartFileByteSource(file), expireIn);
		WorkspaceUploadApplication application = Application.defaultInit(new WorkspaceUploadApplication(), currentUser(),
				UploadFile.fromTempFile(folder.getId(), name, tfKey));
		application.setWorkspace(workspace);
		application.setExpireAt(expireAt);
		applicationService.save(application);
		return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
	}

	/**
	 * 受限上传md5
	 * 
	 * @param workspace
	 * @param folder
	 * @param name
	 * @param md5
	 * @return
	 * @throws IOException
	 */
	@RequiresPermissions("workspace:limitedUpload")
	@RequestMapping(value = "/limitedUpload", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String limitedUpload(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile folder,
			@RequestParam String name, @RequestParam String md5) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		spaceService.checkMd5FileExists(md5);
		long expireIn = globalConfigService.getConfigAsInteger(Configs.Keys.APPLY_UPLOAD_EXPIRES, Configs.Defaults.APPLY_UPLOAD_EXPIRES)
				* DateUtils.MILLIS_PER_DAY;
		long expireAt = System.currentTimeMillis() + expireIn;
		WorkspaceUploadApplication application = Application.defaultInit(new WorkspaceUploadApplication(), currentUser(),
				UploadFile.fromMd5(folder.getId(), name, md5));
		application.setWorkspace(workspace);
		application.setExpireAt(expireAt);
		applicationService.save(application);
		return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
	}

	/**
	 * 受限上传临时文件片段（断点续传）
	 */
	@RequiresPermissions("workspace:limitedUpload")
	@RequestMapping(value = "/limitedUpload", method = RequestMethod.POST, params = "type=fragment", headers = "content-type=multipart/*")
	@ResponseBody
	public String limitedUploadFileFragment(@ModelAttribute("workspace") Workspace workspace, @RequestParam MultipartFile fileFragment)
			throws IOException {
		long expireIn = globalConfigService.getConfigAsInteger(Configs.Keys.APPLY_UPLOAD_EXPIRES, Configs.Defaults.APPLY_UPLOAD_EXPIRES)
				* DateUtils.MILLIS_PER_DAY;
		long expireAt = System.currentTimeMillis() + expireIn;
		String tfKey = tfsService.saveTempFile(new MultipartFileByteSource(fileFragment), expireIn);
		return gson().toJson(new TempFile(tfKey, expireAt));
	}

	/**
	 * 受限合并临时文件片段（断点续传）
	 */
	@RequiresPermissions("workspace:limitedUpload")
	@RequestMapping(value = "/limitedUpload", method = RequestMethod.POST, params = "fragmentIds")
	@ResponseBody
	public String limitedMergeFileFragments(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile folder,
			@RequestParam String name, @RequestParam final String[] fragmentIds) throws IOException {
		if (!folder.getIsDir()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		spaceService.checkFileExtenstion(name);
		ByteSource byteSource = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return new SequenceInputStream(new Enumeration<InputStream>() {
					private int index = 0;

					@Override
					public boolean hasMoreElements() {
						return index < fragmentIds.length;
					}

					@Override
					public InputStream nextElement() {
						try {
							return tfsService.getTempFile(fragmentIds[index++]).openStream();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
		};
		long expireIn = globalConfigService.getConfigAsInteger(Configs.Keys.APPLY_UPLOAD_EXPIRES, Configs.Defaults.APPLY_UPLOAD_EXPIRES)
				* DateUtils.MILLIS_PER_DAY;
		long expireAt = System.currentTimeMillis() + expireIn;
		String tfKey = tfsService.saveTempFile(byteSource, expireIn);
		WorkspaceUploadApplication application = Application.defaultInit(new WorkspaceUploadApplication(), currentUser(),
				UploadFile.fromTempFile(folder.getId(), name, tfKey));
		application.setWorkspace(workspace);
		application.setExpireAt(expireAt);
		applicationService.save(application);
		return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
	}

	/**
	 * 下载最新版本
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "content", method = RequestMethod.GET)
	@ResponseBody
	public void downloadHeadVersion(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("workspace") Workspace workspace,
			@ModelAttribute("file") SpaceFile file) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
		}
		checkAccessible(file);
		downloadFileVersion(file.getHeadVersion(), request, response);

		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, file.getHeadVersion(), SpaceFileVersionEvent.EVENT_DOWNLOAD));
		
		fileService.incrDownloads(file, 1);
	}

	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "downloadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateHeadVersionDownloadTicket(@ModelAttribute("file") SpaceFile file) {
		if (file.getIsDir()) {
			throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
		}
		checkAccessible(file);
		DownloadTicketDto result = fcService.generateDownloadTicket(file.getHeadVersion());
		fileService.incrDownloads(file, 1);
		return gson().toJson(result);
	}

	/**
	 * 获取所有版本信息
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "versions", method = RequestMethod.GET)
	@ResponseBody
	public String getAllVersions(@ModelAttribute("file") SpaceFile file) {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		return gson().toJson(Lists.transform(file.getVersions(), SpaceFileVersionDto.defaultTransformer));
	}

	/**
	 * 获取单个版本信息
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "versions/{ver}", method = RequestMethod.GET)
	@ResponseBody
	public String getVersion(@ModelAttribute("file") SpaceFile file, @PathVariable Integer ver) {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		SpaceFileVersion fv = file.getVersion(ver);
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}

	/**
	 * 上传新版本（multipart）
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "versions", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadNewVersion(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file,
			@RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		checkEditable(file);
		SpaceFileVersion fv = spaceService.saveNewFileVersion(currentUser(), file, null, new MultipartFileByteSource(multipartFile));
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}

	/**
	 * 上传新版本（md5）
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "versions", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String uploadNewVersionByMd5(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file,
			@RequestParam String md5) throws IOException {
		spaceService.checkMd5FileExists(md5);
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		checkEditable(file);
		SpaceFileVersion fv = spaceService.saveNewFileVersion(currentUser(), file, md5, null);
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, fv, SpaceFileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}

	/**
	 * 受限上传新版本（multipart）
	 */
	@RequiresPermissions("workspace:limitedUpload")
	@RequestMapping(value = "versions/limitedUpload", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String limitedUploadNewVersion(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file,
			@RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		long expireIn = globalConfigService.getConfigAsInteger(Configs.Keys.APPLY_UPLOAD_EXPIRES, Configs.Defaults.APPLY_UPLOAD_EXPIRES)
				* DateUtils.MILLIS_PER_DAY;
		long expireAt = System.currentTimeMillis() + expireIn;
		String tfKey = tfsService.saveTempFile(new MultipartFileByteSource(multipartFile), expireIn);
		WorkspaceUploadVersionApplication application = Application.defaultInit(new WorkspaceUploadVersionApplication(), currentUser(),
				UploadFileVersion.formTempFile(file.getId(), tfKey));
		application.setWorkspace(workspace);
		application.setExpireAt(expireAt);
		applicationService.save(application);
		return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
	}

	/**
	 * 上传新版本（md5）
	 */
	@RequiresPermissions("workspace:limitedUpload")
	@RequestMapping(value = "versions/limitedUpload", method = RequestMethod.POST, params = "md5")
	@ResponseBody
	public String limitedUploadNewVersionByMd5(@ModelAttribute("workspace") Workspace workspace, @ModelAttribute("file") SpaceFile file,
			@RequestParam String md5) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		spaceService.checkMd5FileExists(md5);
		long expireIn = globalConfigService.getConfigAsInteger(Configs.Keys.APPLY_UPLOAD_EXPIRES, Configs.Defaults.APPLY_UPLOAD_EXPIRES)
				* DateUtils.MILLIS_PER_DAY;
		long expireAt = System.currentTimeMillis() + expireIn;
		WorkspaceUploadVersionApplication application = Application.defaultInit(new WorkspaceUploadVersionApplication(), currentUser(),
				UploadFileVersion.formMd5(file.getId(), md5));
		application.setWorkspace(workspace);
		application.setExpireAt(expireAt);
		applicationService.save(application);
		return gson().toJson(WorkspaceApplicationDto.defaultTransformer.apply(application));
	}

	/**
	 * 下载文件版本
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "versions/{ver}/content", method = RequestMethod.GET)
	@ResponseBody
	public void downloadFileVersion(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("workspace") Workspace workspace,
			@ModelAttribute("file") SpaceFile file, @PathVariable Integer ver) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		SpaceFileVersion fv = file.getVersion(ver);
		if (fv == null) {
			throw new RestException(ErrorCode.FILE_VERSION_NOT_FOUND);
		}
		downloadFileVersion(fv, request, response);
		postEvent(new SpaceFileVersionEvent(getUserPrincipal(), workspace, fv, SpaceFileVersionEvent.EVENT_DOWNLOAD));
		
		fileService.incrDownloads(file, 1);
	}

	/**
	 * 产生下载文件版本的ticket
	 */
	@RequiresPermissions("workspace:download")
	@RequestMapping(value = "versions/{ver}/downloadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateFileVersionDownloadTicket(@ModelAttribute("file") SpaceFile file, @PathVariable Integer ver) {
		if (file.getIsDir()) {
			throw new RestException(WorkspaceUtils.ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		SpaceFileVersion fv = file.getVersion(ver);
		if (fv == null) {
			throw new RestException(ErrorCode.FILE_VERSION_NOT_FOUND);
		}
		DownloadTicketDto result = fcService.generateDownloadTicket(fv);
		
		fileService.incrDownloads(fv.getFile(), 1);
		
		return gson().toJson(result);
	}

	/**
	 * 产生上传新文件的ticket
	 */
	@RequiresPermissions("workspace:upload")
	@RequestMapping(value = "/uploadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateFileUploadTicket(@ModelAttribute("file") SpaceFile file) throws IOException {
		if (!file.getIsDir()) {
			throw new RestException(ErrorCodes.FOLDER_EXPECTED);
		}
		checkAccessible(file);
		return gson().toJson(fcService.generateFileUploadTicket(file, currentUserId()));
	}

	/**
	 * 产生上传新版本的ticket
	 */
	@RequestMapping(value = "/versions/uploadTickets", method = RequestMethod.POST)
	@ResponseBody
	public String generateFileVersionUploadTicket(@ModelAttribute("file") SpaceFile file) throws IOException {
		if (file.getIsDir()) {
			throw new RestException(ErrorCodes.FILE_EXPECTED);
		}
		checkEditable(file);
		return gson().toJson(fcService.generateFileVersionUploadTicket(file, currentUserId()));
	}

	/**
	 * 添加評論
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	@RequestMapping(value = "/comment", method = RequestMethod.POST)
	@ResponseBody
	public String addComment(@ModelAttribute("file") SpaceFile file, @Valid FileCommentCreateDto creation) {
		User creater = currentUser();
		if (file.getIsDir()) {
			throw new RestException(ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		FileComment comment = fileService.addComment(creater, file, creation);
		if (comment.getAt() != null && comment.getAt().size() > 0) {
			String message = new StringBuilder().append(getUserPrincipal().getUserName()).append("在评论文件:").append(file.getName()).append("的时候@了你")
					.toString();
			sendNotif(new Notification(NotifType.COMMENT_AT, message, FileCommentDto.defaultTransformer.apply(comment), Lists.transform(
					comment.getAt(), Functions.IDENTITY_TO_ID)));
		}
		return gson().toJson(FileCommentDto.defaultTransformer.apply(comment));
	}

	/**
	 * 獲取品論列表
	 * 
	 * @param file
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "/comment/list", method = RequestMethod.GET)
	@ResponseBody
	public String commentList(@ModelAttribute("file") SpaceFile file, @RequestParam(required = false) String q, PageParam pageParam) {
		checkAccessible(file);
		List<SearchFilter> filters = decodeFilters(q);
		if (StringUtils.isEmpty(pageParam.getSort())) {
			pageParam.setSort("-createTime");
		}
		filters.add(new SearchFilter("file", Operator.EQ, file));
		Page<FileComment> page = fileService.commentList(filters, pageParam.getPageable(FileComment.class));
		return Gsons.filterByFields(FileCommentDto.class, pageParam.getFields()).toJson(PageDto.of(page, FileCommentDto.defaultTransformer));
	}

	/**
	 * 添加标签
	 * 
	 * @param file
	 * @param tags
	 * @return
	 */
	@RequestMapping(value = "tags", method = RequestMethod.POST)
	@ResponseBody
	public String addTags(@ModelAttribute("file") SpaceFile file, long[] tags) {
		// 文件夹不支持标签功能
		if (file.getIsDir()) {
			throw new RestException(ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		spaceService.addTags(file, tags);
		return ok();
	}

	/**
	 * 删除标签
	 * 
	 * @param file
	 * @param tags
	 * @return
	 */
	@RequestMapping(value = "tags", method = RequestMethod.DELETE)
	@ResponseBody
	public String removeTags(@ModelAttribute("file") SpaceFile file, long[] tags) {
		// 文件夹不支持标签功能
		if (file.getIsDir()) {
			throw new RestException(ErrorCodes.FILE_EXPECTED);
		}
		checkAccessible(file);
		spaceService.removeTags(file, tags);
		return ok();
	}
}