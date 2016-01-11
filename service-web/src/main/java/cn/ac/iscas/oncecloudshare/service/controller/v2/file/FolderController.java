package cn.ac.iscas.oncecloudshare.service.controller.v2.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileRenameEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidPathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FolderService;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.primitives.Longs;

@Controller
@RequestMapping(value = "/api/v2/folders", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class FolderController extends BaseController {

	@Autowired
	protected FolderService folderService;

	protected File findFolder(String id) {
		long userId = currentUserId();
		if (id.equals("root")) {
			return folderService.findRoot(userId);
		} else if (id.equals(FolderService.BACKUP_PATH)) {
			return folderService.findBackupDir(userId);
		}
		return findFolder(Longs.tryParse(id));
	}

	protected File findFolder(long id) {
		long userId = currentUserId();
		File folder = folderService.findFolder(id);
		if (folder == null) {
			throw new RestException(404, "folder not exists");
		}
		if (folder.getOwner().getId().equals(userId) == false) {
			throw new RestException(403, "permission denied");
		}
		return folder;
	}

	@RequestMapping(value = "{id:root|_bak|\\d+}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String id) {
		return gson().toJson(FileDto.of(findFolder(id)));
	}

	@RequestMapping(value = "{id:root|_bak|\\d+}/children", method = RequestMethod.GET)
	@ResponseBody
	public String getChildren(@PathVariable String id, PageParam pageParam) {
		File folder = findFolder(id);
		Page<File> chilren = folderService.findChildren(folder.getId(), pageParam.getPageable(File.class));
		return Gsons.filterByFields(FileDto.class, pageParam.getFields()).toJson(PageDto.of(chilren, FileDto.TRANSFORMER));
	}

	@RequestMapping(value = "", method = RequestMethod.POST, params = {"parentId", "name"})
	@ResponseBody
	public String makeFolder(@RequestParam Long parentId, String name) {
		File parent = findFolder(parentId);
		File folder = folderService.makeFolder(currentUserId(), parent.getId(), name);
		postEvent(new FileEvent(getUserPrincipal(), folder, FileEvent.EVENT_MAKE_FOLDER));
		return gson().toJson(FileDto.of(folder));
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, params = {"path"})
	@ResponseBody
	public String makeFolder(String path){
		path = FilePathUtil.normalizePath(path);
		if (!path.startsWith(FilePathUtil.ROOT_PATH) || path.startsWith("//")){
			throw new InvalidPathException("invalid path: ".concat(path));
		}
		File folder = folderService.makeFolder(currentUserId(), path);
		postEvent(new FileEvent(getUserPrincipal(), folder, FileEvent.EVENT_MAKE_FOLDER));
		return gson().toJson(FileDto.of(folder));
	}

	@RequestMapping(value = "{fileId:\\d+}", params = "parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String move(@PathVariable Long fileId, @RequestParam Long parentId, @RequestParam String name) {
		File file = findFolder(fileId);
		File parent = findFolder(parentId);
		FileEvent event = null;
		if (file.getParent().getId().equals(parentId)) {
			event = new FileRenameEvent(getUserPrincipal(), file);
		} else {
			event = new FileMoveEvent(getUserPrincipal(), file);
		}
		folderService.move(fileId, parentId, name);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);

	}

	@RequestMapping(value = "{fileId:\\d+}", params = "!parentId", method = RequestMethod.PUT)
	@ResponseBody
	public String updateInfo(@PathVariable Long fileId, @RequestParam(required = false) String description,
			@RequestParam(required = false) Boolean favorite) {
		File file = findFolder(fileId);
		FileEvent event = new FileUpdateEvent(getUserPrincipal(), file);
		folderService.updateInfo(fileId, description, favorite);
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "{fileId:\\d+}/trash", method = RequestMethod.PUT)
	@ResponseBody
	public String trash(@PathVariable Long fileId) {
		File file = findFolder(fileId);
		folderService.trash(fileId);
		postEvent(new FileEvent(getUserPrincipal(), file, FileEvent.EVENT_TRASH));
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "{fileId:\\d+}/untrash", method = RequestMethod.PUT)
	@ResponseBody
	public String untrash(@PathVariable Long fileId, @RequestParam(required = false) Long parentId,
			@RequestParam(required = false) String name) {
		File file = findFolder(fileId);
		FileEvent event = new FileUntrashEvent(getUserPrincipal(), file);
		if (parentId == null || name == null) {
			folderService.untrash(fileId);
		} else {
			File parent = findFolder(parentId);
			folderService.untrashTo(fileId, parentId, name);
		}
		postEvent(event);
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "{fileId:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long fileId) {
		File file = findFolder(fileId);
		folderService.delete(fileId);
		postEvent(new FileEvent(getUserPrincipal(), file, FileEvent.EVENT_DELETE));
		return gson().toJson(ResponseDto.OK);
	}
}
