package cn.ac.iscas.oncecloudshare.service.extensions.company.space.events;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.service.shiro.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

/**
 * <pre>
 * 1	上传	EVENT_UPLOAD
 * 2	下载	EVENT_UPLOAD
 * 3	删除	EVENT_DELETE
 * </pre>
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Interceptable
public class SpaceFileVersionEvent extends UserRequestEvent {
	/**
	 * 上传事件
	 */
	public static final int EVENT_UPLOAD = 1;
	/**
	 * 下载事件
	 */
	public static final int EVENT_DOWNLOAD = EVENT_UPLOAD + 1;
	/**
	 * 删除事件
	 */
	public static final int EVENT_DELETE = EVENT_DOWNLOAD + 1;

	private final BaseSpace space;
	private final SpaceFileVersion fileVersion;
	private final int eventType;

	public SpaceFileVersionEvent(UserPrincipal principal, BaseSpace space, SpaceFileVersion fileVersion, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.space = space;
		this.fileVersion = fileVersion;
		this.eventType = eventType;
	}

	public BaseSpace getSpace() {
		return space;
	}

	public SpaceFileVersion getFileVersion() {
		return fileVersion;
	}

	public int getEventType() {
		return eventType;
	}
}
