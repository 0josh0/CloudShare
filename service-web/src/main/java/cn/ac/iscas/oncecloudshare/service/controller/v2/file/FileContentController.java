package cn.ac.iscas.oncecloudshare.service.controller.v2.file;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.ac.iscas.oncecloudshare.service.controller.v2.MultipartFileByteSource;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileVersionDto;
import cn.ac.iscas.oncecloudshare.service.event.file.FileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.DownloadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UploadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.TempFileStorageService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FolderService;

import com.google.common.base.Objects;
import com.google.common.io.ByteSource;

@Controller
@RequestMapping(value = "/contentapi/v2/")
public class FileContentController extends BaseFileController {

	@Autowired
	FileService fileService;

	@Autowired
	FolderService folderService;

	@Autowired
	TempFileStorageService tfsService;

	private ByteSource findFileByteSource(String key) throws IOException {
		ByteSource source = runtimeContext.getFileStorageService().retrieveFileContent(key);
		if (source == null) {
			// 如果找不到，就去临时文件里找
			source = tfsService.getTempFile(key);
		}
		if (source == null) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		return source;
	}

	/**
	 * 下载文件版本
	 */
	@RequiresPermissions("principal:download")
	@RequestMapping(value = "files", method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
		DownloadPrincipal principal = (DownloadPrincipal) getPrincipal();

		ByteSource byteSource = findFileByteSource(principal.key);
		initDownload(request, response, byteSource, principal.filename);
	}

	/**
	 * 上传新文件
	 */
	@RequiresPermissions("principal:upload")
	@RequestMapping(value = "files", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String upload(HttpServletRequest request, @RequestParam(required = false) String name,
			@RequestParam(value = "file") MultipartFile file) throws IOException {
		UploadPrincipal principal = (UploadPrincipal) getPrincipal();
		if (file.isEmpty()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		File parent = findParent(principal.parentId, false);
		name = Objects.firstNonNull(name, file.getOriginalFilename());
		checkFileExtenstion(name);
		FileVersion fv = fileService.saveNewFile(parent.getOwner().getId(), principal.parentId, name, null, new MultipartFileByteSource(
				file));
		// 发送文件上传的事件
		postEvent(new FileVersionEvent(new UserPrincipal(parent.getOwner(), this.currentUserId()), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileDto.of(fv.getFile()));
	}

	/**
	 * 上传新版本
	 */
	@RequiresPermissions("principal:upload")
	@RequestMapping(value = "fileVersions", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadNewVersion(@RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
		UploadPrincipal principal = (UploadPrincipal) getPrincipal();
		if (multipartFile.isEmpty()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}

		File file = findFile(principal.fileId, false);
		FileVersion fv = fileService.saveNewFileVersion(file.getId(), null, new MultipartFileByteSource(multipartFile));
		// 发送文件上传的事件
		postEvent(new FileVersionEvent(new UserPrincipal(file.getOwner(), this.currentUserId()), fv, FileVersionEvent.EVENT_UPLOAD));
		return gson().toJson(FileVersionDto.of(fv));
	}
}
