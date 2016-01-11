package cn.ac.iscas.oncecloudshare.service.extensions.company.space.listeners;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.events.SpaceFileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.WorkspaceNotifType;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.ThreadSafeListener;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

//@Component
public class NotifSubscribers {
	private static NotifSubscribers instance;

	@Resource
	private RuntimeContext runtimeContext;

	public NotifSubscribers() {
		if (instance != null) {
			throw new IllegalArgumentException("只能有一个WorkspaceMemberListeners实例!");
		}
		instance = this;
	}

	public static NotifSubscribers getInstance() {
		return instance;
	}

//	@SubscribeEvent
//	@ThreadSafeListener
//	public void handleEvent(SpaceFileEvent event) {
//		NotificationType type = null;
//		Map<String, Object> content = getCommonContent(event);
//		List<Long> to = null;
//		switch (event.getEventType()) {
//		case SpaceFileEvent.EVENT_CLEAR_TRASH:
//			type = WorkspaceNotifType.CLEAR_TRASH;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		case SpaceFileEvent.EVENT_DELETE:
//			type = WorkspaceNotifType.FILE_DELETED;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		case SpaceFileEvent.EVENT_MAKE_FOLDER:
//			type = WorkspaceNotifType.FOLDER_CREATED;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		case SpaceFileEvent.EVENT_MOVE:
//			type = WorkspaceNotifType.FILE_MOVED;
//			to = event.getWorkspace().getMemberUserIds();
//			SpaceFileMoveEvent moveEvent = (SpaceFileMoveEvent) event;
//			content.put("oldPath", FilePathUtil.concatPath(moveEvent.getOldParent().getPath(), moveEvent.getOldName()));
//			content.put("newPath", moveEvent.getFile().getPath());
//			break;
//		case SpaceFileEvent.EVENT_TRASH:
//			type = WorkspaceNotifType.FILE_TRASHED;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		case SpaceFileEvent.EVENT_UNTRASH:
//			type = WorkspaceNotifType.FILE_UNTRASHED;
//			to = event.getWorkspace().getMemberUserIds();
//			SpaceFileUntrashEvent untrashEvent = (SpaceFileUntrashEvent) event;
//			content.put("oldPath", FilePathUtil.concatPath(untrashEvent.getOldParent().getPath(), untrashEvent.getOldName()));
//			content.put("newPath", untrashEvent.getFile().getPath());
//			break;
//		case SpaceFileEvent.EVENT_UPDATE:
//			type = WorkspaceNotifType.FILE_UPDATED;
//			to = event.getWorkspace().getMemberUserIds();
//			SpaceFileUpdateEvent updateEvent = (SpaceFileUpdateEvent) event;
//			content.put("oldDescription", updateEvent.getOldDescription());
//			content.put("newDescription", updateEvent.getFile().getDescription());
//			break;
//		default:
//			break;
//		}
//		if (type != null && Collections3.isNotEmpty(to)) {
//			sendNotif(type, content, to);
//		}
//	}
//
//	@SubscribeEvent
//	@ThreadSafeListener
//	public void handleEvent(SpaceFileVersionEvent event) {
//		NotificationType type = null;
//		Map<String, Object> content = getCommonContent(event);
//		List<Long> to = null;
//		switch (event.getEventType()) {
//		case SpaceFileVersionEvent.EVENT_DELETE:
//			type = WorkspaceNotifType.FILE_DELETED;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		case SpaceFileVersionEvent.EVENT_DOWNLOAD:
//			type = WorkspaceNotifType.FILE_DOWNLOADED;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		case SpaceFileVersionEvent.EVENT_UPLOAD:
//			type = WorkspaceNotifType.FILE_UPLOADED;
//			to = event.getWorkspace().getMemberUserIds();
//			break;
//		default:
//			break;
//		}
//		if (type != null && Collections3.isNotEmpty(to)) {
//			sendNotif(type, content, to);
//		}
//	}
//
//	private Map<String, Object> getCommonContent(WorkspaceEvent event) {
//		Map<String, Object> content = Maps.newHashMap();
//		// workspace
//		WorkspaceDto workspace = new WorkspaceDto();
//		workspace.id = event.getWorkspace().getId();
//		workspace.name = event.getWorkspace().getName();
//		content.put("workspace", workspace);
//		// master
//		UserDto master = new UserDto();
//		master.id = event.getPrincipal().getUserId();
//		master.name = event.getPrincipal().getUserName();
//		content.put("master", master);
//
//		return content;
//	}
//
//	protected Map<String, Object> getCommonContent(MemberEvent event) {
//		Map<String, Object> content = Maps.newHashMap();
//		// workspace
//		WorkspaceDto workspace = new WorkspaceDto();
//		workspace.id = event.getWorkspace().getId();
//		workspace.name = event.getWorkspace().getName();
//		content.put("workspace", workspace);
//		// master
//		UserDto master = new UserDto();
//		master.id = event.getPrincipal().getUserId();
//		master.name = event.getPrincipal().getUserName();
//		content.put("master", master);
//		// slave
//		UserDto slave = new UserDto();
//		slave.id = event.getMember().getUser().getId();
//		slave.name = event.getMember().getUser().getName();
//		content.put("slave", slave);
//
//		return content;
//	}
//
//	private Map<String, Object> getCommonContent(SpaceFileEvent event) {
//		Map<String, Object> content = Maps.newHashMap();
//		// workspace
//		WorkspaceDto workspace = new WorkspaceDto();
//		workspace.id = event.getWorkspace().getId();
//		workspace.name = event.getWorkspace().getName();
//		content.put("workspace", workspace);
//		// master
//		UserDto master = new UserDto();
//		master.id = event.getPrincipal().getUserId();
//		master.name = event.getPrincipal().getUserName();
//		content.put("master", master);
//		// file
//		if (event.getFile() != null) {
//			SpaceFileDto file = new SpaceFileDto();
//			file.id = event.getFile().getId();
//			file.name = event.getFile().getName();
//			content.put("file", file);
//		}
//
//		return content;
//	}
//
//	private Map<String, Object> getCommonContent(SpaceFileVersionEvent event) {
//		Map<String, Object> content = Maps.newHashMap();
//		// workspace
//		WorkspaceDto workspace = new WorkspaceDto();
//		workspace.id = event.getWorkspace().getId();
//		workspace.name = event.getWorkspace().getName();
//		content.put("workspace", workspace);
//		// master
//		UserDto master = new UserDto();
//		master.id = event.getPrincipal().getUserId();
//		master.name = event.getPrincipal().getUserName();
//		content.put("master", master);
//		// file
//		if (event.getFileVersion() != null) {
//			SpaceFileDto file = new SpaceFileDto();
//			file.id = event.getFileVersion().getFile().getId();
//			file.name = event.getFileVersion().getFile().getName();
//			content.put("file", file);
//			content.put("version", event.getFileVersion().getVersion());
//		}
//
//		return content;
//	}
//
//	protected void sendNotif(Notification notification) {
//		runtimeContext.getNotifService().sendNotif(notification);
//	}
//
//	protected void sendNotif(NotificationType type, Object content, List<Long> to) {
//		sendNotif(new Notification(type, Gsons.defaultGsonNoPrettify().toJson(content), to));
//	}
//
//	protected void sendNotif(NotificationType type, Object content, Long to) {
//		sendNotif(new Notification(type, Gsons.defaultGsonNoPrettify().toJson(content), to));
//	}
}
