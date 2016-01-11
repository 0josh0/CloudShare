package cn.ac.iscas.oncecloudshare.service.system;

import java.util.List;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;

import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;

public abstract class CommonComponent {
	@Resource
	protected RuntimeContext runtimeContext;

	/**
	 * 当前接口调用者的身份
	 * 
	 * @return
	 */
	protected Object getPrincipal() {
		return SecurityUtils.getSubject().getPrincipal();
	}

	/**
	 * 如果不是UserPrincipal，将返回null
	 * 
	 * @return
	 */
	protected UserPrincipal getUserPrincipal() {
		Object principal = getPrincipal();
		return (principal instanceof UserPrincipal) ? (UserPrincipal) principal : null;
	}

	/**
	 * 当前用户id，如果没登陆，将返回null
	 * 
	 * @return
	 */
	protected Long currentUserId() {
		UserPrincipal principal = getUserPrincipal();
		return principal == null ? null : getUserPrincipal().getUserId();
	}

	/**
	 * 是否是已登录的用户
	 * 
	 * @return
	 */
	protected boolean isAuthenticatedUser() {
		return getUserPrincipal() != null;
	}

	protected void postEvent(Object event) {
		runtimeContext.getEventBus().post(event);
	}

	protected void sendNotif(Notification notification) {
		runtimeContext.getNotifService().sendNotif(notification);
	}

	protected void sendNotif(NotificationType type, String content, Object attributes, List<Long> to) {
		runtimeContext.getNotifService().sendNotif(new Notification(type, content, attributes, to));
	}

	protected void sendNotif(NotificationType type, String content, Object attributes, Long to) {
		runtimeContext.getNotifService().sendNotif(new Notification(type, content, attributes, to));
	}
}
