package cn.ac.iscas.oncecloudshare.service.action.log;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.action.SystemActionType;
import cn.ac.iscas.oncecloudshare.service.action.SystemTargetType;
import cn.ac.iscas.oncecloudshare.service.action.log.annotations.ActionLogger;
import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileRenameEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.event.file.FileEvent.FileType;
import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;

@Component
public class SystemLoggers {
	@Resource
	private UserService userService;

	@ActionLogger
	public boolean logFileEvent(FileEvent event, ActionLog log) {
		log.setUser(userService.find(event.getPrincipal().getUserId()));
		log.setTargetType(SystemTargetType.FILE.getCode());
		if (event.getFile() != null) {
			log.setTargetId(event.getFile().getId().toString());
		}
		switch (event.getEventType()) {
		case FileEvent.EVENT_MAKE_FOLDER:
			log.setType(SystemActionType.FILE_MAKEFOLDER.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "创建了文件夹" + event.getFile().getPath());
			break;
		case FileEvent.EVENT_TRASH:
			log.setType(SystemActionType.FILE_TRASH.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "将" + event.getFile().getPath() + "放入了回收站");
			break;
		case FileEvent.EVENT_DELETE:
			log.setType(SystemActionType.FILE_DELETE.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "彻底删除了" + event.getFile().getPath());
			break;
		case FileEvent.EVENT_CLEAR_TRASH:
			log.setType(SystemActionType.FILE_CLEARTRASH.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "清空了回收站");
			break;
		}
		return true;
	}

	@ActionLogger
	public boolean logFileEvent(FileMoveEvent event, ActionLog log) {
		log.setType(SystemActionType.FILE_MOVE.getCode());
		String oldPath = FilePathUtil.concatPath(event.getOldParent().getPath(), event.getOldName());
		log.addParam("oldPath", oldPath);
		log.addParam("newPath", event.getFile().getPath());
		log.setDescription(event.getPrincipal().getUserName() + "将" + event.getFileType().typeName() + event.getOldName() + "移动到"
				+ FileType.DIR.typeName() + event.getFile().getParent().getPath());
		return true;
	}

	@ActionLogger
	public boolean logFileEvent(FileRenameEvent event, ActionLog log) {
		log.setType(SystemActionType.FILE_RENAME.getCode());
		String oldPath = FilePathUtil.concatPath(event.getFile().getPath(), event.getOldName());
		log.addParam("oldPath", oldPath);
		log.addParam("newPath", event.getFile().getPath());
		log.setDescription(event.getPrincipal().getUserName() + "将" + event.getFileType().typeName() + event.getOldName() + "重命名为"
				+ event.getFileType().typeName() + event.getFile().getName());
		return true;
	}

	@ActionLogger
	public boolean logFileEvent(FileUntrashEvent event, ActionLog log) {
		log.setType(SystemActionType.FILE_UNTRASH.getCode());
		String oldPath = FilePathUtil.concatPath(event.getOldParent().getPath(), event.getOldName());
		log.addParam("oldPath", oldPath);
		log.addParam("newPath", event.getFile().getPath());
		log.setDescription(event.getPrincipal().getUserName() + "从回收站中还原了" + event.getFileType().typeName() + event.getOldName() + "到"
				+ event.getFile().getPath());
		return true;
	}

	@ActionLogger
	public boolean logFileEvent(FileUpdateEvent event, ActionLog log) {
		log.setType(SystemActionType.FILE_UPDATE.getCode());
		// 执行了(取消)收藏操作
		if (!event.getFile().getFavorite().equals(event.getOldFavorite())) {
			if (event.getFile().getFavorite()) {
				log.setDescription(event.getPrincipal().getUserName() + "收藏了" + event.getFile().getPath());
			} else {
				log.setDescription(event.getPrincipal().getUserName() + "取消了收藏" + event.getFile().getPath());
			}
			log.addParam("favorite", event.getFile().getFavorite());
		} else {
			log.setDescription(event.getPrincipal().getUserName() + "更新了" + event.getFile().getPath() + "的元数据");
		}
		if (!StringUtils.equals(event.getOldDescription(), event.getFile().getDescription())) {
			log.addParam("oldDesc", event.getOldDescription());
			log.addParam("newDesc", event.getFile().getDescription());
		}
		return true;
	}

	@ActionLogger
	public boolean logFileVersionEvent(FileVersionEvent event, ActionLog log) {
		log.setUser(userService.find(event.getPrincipal().getUserId()));
		log.setTargetType(SystemTargetType.FILE.getCode());
		log.setTargetId(event.getFileVersion().getFile().getId().toString());
		log.addParam("version", event.getFileVersion().getVersion());
		switch (event.getEventType()) {
		// 下载
		case FileVersionEvent.EVENT_DOWNLOAD:
			log.setType(SystemActionType.FILE_DOWLOAD.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "下载了" + event.getFileVersion().getFile().getPath());
			break;
		// 上传
		case FileVersionEvent.EVENT_UPLOAD:
			log.setType(SystemActionType.FILE_UPLOAD.getCode());
			if (event.getFileVersion().getVersion() == 0) {
				log.setDescription(event.getPrincipal().getUserName()
						+ "上传了新文件:"
						+ event.getFileVersion().getFile().getName()
						+ (event.getFileVersion().getFile().getParent().getPath().endsWith("/") == true ? "" : "到"
								+ event.getFileVersion().getFile().getParent().getPath()));
			} else {
				log.setDescription(event.getPrincipal().getUserName() + "上传了新版本到" + event.getFileVersion().getFile().getPath());
			}
			break;
		// 删除
		case FileVersionEvent.EVENT_DELETE:
			log.setType(SystemActionType.FILE_DELETE.getCode());
			log.setDescription(event.getPrincipal().getUserName() + "删除了" + event.getFileVersion().getFile().getPath() + "的版本"
					+ event.getFileVersion().getVersion());
			break;
		default:
			break;
		}
		return true;
	}
}