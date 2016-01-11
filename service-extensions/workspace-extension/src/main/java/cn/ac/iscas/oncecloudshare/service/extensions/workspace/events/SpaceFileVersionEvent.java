package cn.ac.iscas.oncecloudshare.service.extensions.workspace.events;

import cn.ac.iscas.oncecloudshare.service.event.VersionEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
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
public class SpaceFileVersionEvent extends VersionEvent {
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

	private final Workspace workspace;
	private final SpaceFileVersion fileVersion;
	private final int eventType;

	public SpaceFileVersionEvent(UserPrincipal principal, Workspace workspace, SpaceFileVersion fileVersion, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.workspace = workspace;
		this.fileVersion = fileVersion;
		this.eventType = eventType;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public SpaceFileVersion getFileVersion() {
		return fileVersion;
	}

	public int getEventType() {
		return eventType;
	}
}
