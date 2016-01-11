package cn.ac.iscas.oncecloudshare.service.extensions.workspace.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.WorkspaceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.ApplicationEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.MemberChangeRoleEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.MemberEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileMoveEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileUntrashEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileUpdateEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.SpaceFileVersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.events.WorkspaceEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceJoinApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceNotifType;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceUploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.ThreadSafeListener;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
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

	@SubscribeEvent
	@ThreadSafeListener
	public void handleEvent(WorkspaceEvent event) {
//		NotificationType type = null;
//		Map<String, Object> content = getCommonContent(event);
//		List<Long> to = null;
//		switch (event.getEventType()) {
//		// 审核
//		case WorkspaceEvent.EVENT_REVIEWED:
//			type = WorkspaceNotifType.APP_REVIEWED;
//			to = ImmutableList.<Long> of(event.getWorkspace().getApplyBy().getId());
//			content.put("agreed", ((WorkspaceReviewEvent) event).isAgreed());
//			break;
//		default:
//			break;
//		}
//		if (type != null && Collections3.isNotEmpty(to)) {
//			sendNotif(type, content, to);
//		}
	}

	@SubscribeEvent
	@ThreadSafeListener
	public void handleEvent(MemberEvent event) {
		NotificationType type = null;
		StringBuilder content = new StringBuilder();
		Map<String, Object> attributes = getCommonContent(event);
		Set<Long> to = Sets.newHashSet();
		switch (event.getEventType()) {
		// 用户加入
		case MemberEvent.EVENT_JOINED:
			type = WorkspaceNotifType.MEMBER_JOINED;
			int idx = 0;
			for (; idx < event.getMembers().size(); idx++) {
				User user = event.getMembers().get(idx).getUser();
				content.append(user.getName()).append(",");
				if (idx > 4) {
					break;
				}
			}
			content.setLength(content.length() - 1);
			if (idx < event.getMembers().size() - 1) {
				content.append("等").append(event.getMembers().size()).append("用户");
			}
			content.append("加入了").append(event.getWorkspace().getName());
			to.addAll(Collections2.filter(Lists.transform(event.getWorkspace().getTeam().getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
			to.addAll(Collections2.filter(Lists.transform(event.getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
			break;
		// 用户被踢
		case MemberEvent.EVENT_KICK:
			type = WorkspaceNotifType.MEMBER_KICKED;

			idx = 0;
			for (; idx < event.getMembers().size(); idx++) {
				User user = event.getMembers().get(idx).getUser();
				content.append(user.getName()).append(",");
				if (idx > 4) {
					break;
				}
			}
			content.setLength(content.length() - 1);
			if (idx < event.getMembers().size() - 1) {
				content.append("等").append(event.getMembers().size()).append("用户");
			}
			content.append("退出了").append(event.getWorkspace().getName());

			to.addAll(Collections2.filter(Lists.transform(event.getWorkspace().getTeam().getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
			to.addAll(Collections2.filter(Lists.transform(event.getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
			break;
		// 用户的角色被改变
		case MemberEvent.EVENT_CHANGE_ROLE:
			MemberChangeRoleEvent changeRoleEvent = (MemberChangeRoleEvent) event;
			type = WorkspaceNotifType.ROLE_CHANGED;

			content.append("您在").append(event.getWorkspace().getName()).append("中的角色被改成了").append(Roles.getDisplayName(changeRoleEvent.getNewRole()));

			to.addAll(Collections2.filter(Lists.transform(event.getMembers(), TeamMate.TO_USERID), Predicates.notNull()));
			attributes.put("newRole", changeRoleEvent.getNewRole());
			break;
		default:
			break;
		}
		if (type != null && Collections3.isNotEmpty(to)) {
			sendNotif(type, content.toString(), attributes, Lists.newArrayList(to));
		}
	}

	@SubscribeEvent
	@ThreadSafeListener
	public void handleEvent(SpaceFileEvent event) {
		NotificationType type = null;
		StringBuilder content = new StringBuilder();
		Map<String, Object> attributes = getCommonContent(event);
		List<Long> to = null;
		switch (event.getEventType()) {
		case SpaceFileEvent.EVENT_CLEAR_TRASH:
			type = WorkspaceNotifType.CLEAR_TRASH;
			to = event.getWorkspace().getMemberUserIds();
			break;
		case SpaceFileEvent.EVENT_DELETE:
			type = WorkspaceNotifType.FILE_DELETED;
			to = event.getWorkspace().getMemberUserIds();
			break;
		case SpaceFileEvent.EVENT_MAKE_FOLDER:
			type = WorkspaceNotifType.FOLDER_CREATED;
			to = event.getWorkspace().getMemberUserIds();
			break;
		case SpaceFileEvent.EVENT_MOVE:
			type = WorkspaceNotifType.FILE_MOVED;
			to = event.getWorkspace().getMemberUserIds();
			SpaceFileMoveEvent moveEvent = (SpaceFileMoveEvent) event;
			attributes.put("oldPath", FilePathUtil.concatPath(moveEvent.getOldParent().getPath(), moveEvent.getOldName()));
			attributes.put("newPath", moveEvent.getFile().getPath());
			break;
		case SpaceFileEvent.EVENT_TRASH:
			type = WorkspaceNotifType.FILE_TRASHED;
			to = event.getWorkspace().getMemberUserIds();
			break;
		case SpaceFileEvent.EVENT_UNTRASH:
			type = WorkspaceNotifType.FILE_UNTRASHED;
			to = event.getWorkspace().getMemberUserIds();
			SpaceFileUntrashEvent untrashEvent = (SpaceFileUntrashEvent) event;
			attributes.put("oldPath", FilePathUtil.concatPath(untrashEvent.getOldParent().getPath(), untrashEvent.getOldName()));
			attributes.put("newPath", untrashEvent.getFile().getPath());
			break;
		case SpaceFileEvent.EVENT_UPDATE:
			type = WorkspaceNotifType.FILE_UPDATED;
			to = event.getWorkspace().getMemberUserIds();
			SpaceFileUpdateEvent updateEvent = (SpaceFileUpdateEvent) event;
			attributes.put("oldDescription", updateEvent.getOldDescription());
			attributes.put("newDescription", updateEvent.getFile().getDescription());
			break;
		default:
			break;
		}
		if (type != null && Collections3.isNotEmpty(to)) {
			sendNotif(type, content.toString(), attributes, to);
		}
	}

	@SubscribeEvent
	@ThreadSafeListener
	public void handleEvent(SpaceFileVersionEvent event) {
		NotificationType type = null;
		Map<String, Object> attributes = getCommonContent(event);
		StringBuilder content = new StringBuilder();
		List<Long> to = null;
		switch (event.getEventType()) {
		case SpaceFileVersionEvent.EVENT_DELETE:
			type = WorkspaceNotifType.FILE_DELETED;
			to = event.getWorkspace().getMemberUserIds();
			break;
		case SpaceFileVersionEvent.EVENT_DOWNLOAD:
			type = WorkspaceNotifType.FILE_DOWNLOADED;
			to = event.getWorkspace().getMemberUserIds();
			break;
		case SpaceFileVersionEvent.EVENT_UPLOAD:
			type = WorkspaceNotifType.FILE_UPLOADED;
			to = event.getWorkspace().getMemberUserIds();
			break;
		default:
			break;
		}
		if (type != null && Collections3.isNotEmpty(to)) {
			sendNotif(type, content.toString(), attributes, to);
		}
	}

	@SubscribeEvent
	@ThreadSafeListener
	public void handleEvent(ApplicationEvent event) {
		NotificationType type = null;
		List<Long> to = null;
		StringBuilder content = new StringBuilder();
		switch (event.getEventType()) {
		case ApplicationEvent.EVENT_CREATED:
			type = WorkspaceNotifType.APP_CREATED;
			List<TeamMate> members = event.getWorkspace().getTeam().getMembers();
			to = Lists.newArrayList();
			for (TeamMate member : members) {
				if (Roles.OWNER.equals(member.getRole()) || Roles.ADMIN.equals(member.getRole())) {
					if (UserStatus.ACTIVE.equals(member.getUser().getStatus())) {
						to.add(member.getUser().getId());
					}
				}
			}
			content.append(event.getApplication().getApplyBy().getName()).append("申请");
			if (event.getApplication() instanceof WorkspaceJoinApplication) {
				content.append("加入").append(event.getApplication().getWorkspace().getName());
			} else if (event.getApplication() instanceof WorkspaceUploadApplication) {
				content.append("上传文件到").append(event.getApplication().getWorkspace().getName());
			} else if (event.getApplication() instanceof WorkspaceUploadVersionApplication) {
				content.append("上传新版本到").append(event.getApplication().getWorkspace().getName());
			}
			break;
		case ApplicationEvent.EVENT_REVIEWED:
			type = WorkspaceNotifType.APP_REVIEWED;
			to = Lists.newArrayList(event.getApplication().getApplyBy().getId());
			
			content.append(event.getApplication().getReviewBy().getName()).append(ApplicationStatus.AGREED.equals(event.getApplication().getStatus()) ? "通过了" : "拒绝了");
			if (event.getApplication() instanceof WorkspaceJoinApplication) {
				content.append("您加入").append(event.getApplication().getWorkspace().getName()).append("的申请");
			} else if (event.getApplication() instanceof WorkspaceUploadApplication) {
				content.append("您上传文件到").append(event.getApplication().getWorkspace().getName()).append("的申请");
			} else if (event.getApplication() instanceof WorkspaceUploadVersionApplication) {
				content.append("您上传新版本到").append(event.getApplication().getWorkspace().getName()).append("的申请");
			}
			break;
		default:
			break;
		}
		UserDto master = new UserDto();
		master.id = event.getPrincipal().getUserId();
		master.name = event.getPrincipal().getUserName();
		if (type != null && Collections3.isNotEmpty(to)) {
			sendNotif(type, content.toString(), WorkspaceApplicationDto.defaultTransformer.apply(event.getApplication()), to);
		}
	}

	private Map<String, Object> getCommonContent(WorkspaceEvent event) {
		Map<String, Object> content = Maps.newHashMap();
		// workspace
		WorkspaceDto workspace = new WorkspaceDto();
		workspace.id = event.getWorkspace().getId();
		workspace.name = event.getWorkspace().getName();
		content.put("workspace", workspace);
		// master
		UserDto master = new UserDto();
		master.id = event.getPrincipal().getUserId();
		master.name = event.getPrincipal().getUserName();
		content.put("master", master);

		return content;
	}

	protected Map<String, Object> getCommonContent(MemberEvent event) {
		Map<String, Object> content = Maps.newHashMap();
		// workspace
		WorkspaceDto workspace = new WorkspaceDto();
		workspace.id = event.getWorkspace().getId();
		workspace.name = event.getWorkspace().getName();
		content.put("workspace", workspace);
		// master
		UserDto master = new UserDto();
		master.id = event.getPrincipal().getUserId();
		master.name = event.getPrincipal().getUserName();
		content.put("master", master);
		// slaves
		List<UserDto> slaves = Lists.newArrayList();
		for (TeamMate member : event.getMembers()) {
			UserDto slave = new UserDto();
			slave.id = member.getUser().getId();
			slave.name = member.getUser().getName();
			slaves.add(slave);
		}
		content.put("slaves", slaves);

		return content;
	}

	private Map<String, Object> getCommonContent(SpaceFileEvent event) {
		Map<String, Object> content = Maps.newHashMap();
		// workspace
		WorkspaceDto workspace = new WorkspaceDto();
		workspace.id = event.getWorkspace().getId();
		workspace.name = event.getWorkspace().getName();
		content.put("workspace", workspace);
		// master
		UserDto master = new UserDto();
		master.id = event.getPrincipal().getUserId();
		master.name = event.getPrincipal().getUserName();
		content.put("master", master);
		// file
		if (event.getFile() != null) {
			SpaceFileDto file = new SpaceFileDto();
			file.id = event.getFile().getId();
			file.name = event.getFile().getName();
			content.put("file", file);
		}

		return content;
	}

	private Map<String, Object> getCommonContent(SpaceFileVersionEvent event) {
		Map<String, Object> content = Maps.newHashMap();
		// workspace
		WorkspaceDto workspace = new WorkspaceDto();
		workspace.id = event.getWorkspace().getId();
		workspace.name = event.getWorkspace().getName();
		content.put("workspace", workspace);
		// master
		UserDto master = new UserDto();
		master.id = event.getPrincipal().getUserId();
		master.name = event.getPrincipal().getUserName();
		content.put("master", master);
		// file
		if (event.getFileVersion() != null) {
			SpaceFileDto file = new SpaceFileDto();
			file.id = event.getFileVersion().getFile().getId();
			file.name = event.getFileVersion().getFile().getName();
			content.put("file", file);
			content.put("version", event.getFileVersion().getVersion());
		}

		return content;
	}

	protected void sendNotif(Notification notification) {
		runtimeContext.getNotifService().sendNotif(notification);
	}

	protected void sendNotif(NotificationType type, String content, Object attributes, List<Long> to) {
		sendNotif(new Notification(type, content, attributes, to));
	}

	protected void sendNotif(NotificationType type, String content, Object attributes, Long to) {
		sendNotif(new Notification(type, content, attributes, to));
	}
}
