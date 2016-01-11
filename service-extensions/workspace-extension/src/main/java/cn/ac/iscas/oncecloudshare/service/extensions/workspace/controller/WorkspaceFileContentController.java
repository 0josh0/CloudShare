package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.MultipartFileByteSource;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileVersionDto;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UploadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

@Controller
@RequestMapping(value = "/contentapi/v2/exts/workspaces")
public class WorkspaceFileContentController extends BaseController {
	@Resource
	private SpaceService spaceService;
	@Resource
	private UserService userService;

	/**
	 * 检查文件扩展名是否禁止
	 * 
	 * @param filename
	 */
	protected void checkFileExtenstion(String filename) {
		String ext = FilenameUtils.getExtension(filename);
		if (Strings.isNullOrEmpty(ext) == false) {
			String forbiddenExts = globalConfigService.getConfig(Configs.Keys.FORBIDDEN_EXT, "");
			for (String forbiddenExt : Splitter.on(',').split(forbiddenExts)) {
				if (forbiddenExt.equals(ext)) {
					throw new RestException(ErrorCode.FORBIDDEN_FILE_EXTENSION);
				}
			}
		}
	}

	/**
	 * 上传新文件
	 */
	@RequiresPermissions("principal:upload")
	@RequestMapping(value = "files", method = RequestMethod.POST, headers = "content-type=multipart/*")
	@ResponseBody
	public String upload(HttpServletRequest request, @RequestParam(required = false) String name, @RequestParam(value = "file") MultipartFile file)
			throws IOException {
		UploadPrincipal principal = (UploadPrincipal) getPrincipal();
		if (file.isEmpty()) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		SpaceFile parent = spaceService.findFolder(principal.parentId);
		if (parent == null) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		name = Objects.firstNonNull(name, file.getOriginalFilename());
		checkFileExtenstion(name);
		SpaceFileVersion fv = spaceService.saveNewFile(userService.find(principal.getUploaderId()), parent, name, null, new MultipartFileByteSource(
				file));
		return gson().toJson(SpaceFileDto.defaultTransformer.apply(fv.getFile()));
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
		SpaceFile file = spaceService.findFile(principal.fileId);
		if (file == null) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		SpaceFileVersion fv = spaceService.saveNewFileVersion(userService.find(principal.getUploaderId()), file, null, new MultipartFileByteSource(
				multipartFile));
		return gson().toJson(SpaceFileVersionDto.defaultTransformer.apply(fv));
	}
}
