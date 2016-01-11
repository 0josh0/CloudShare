package cn.ac.iscas.oncecloudshare.service.event.file;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.event.VersionEvent;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

/**
 * <pre>
 * 1	上传	EVENT_UPLOAD
 * 2	下载	EVENT_UPLOAD
 * 3	删除	EVENT_DELETE</pre>
 * 
 * @author cly
 * @version  
 * @since JDK 1.6
 */
@Interceptable
public class FileVersionEvent extends VersionEvent {
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
	

	private final FileVersion fileVersion;
	private final int eventType;

	public FileVersionEvent(UserPrincipal principal, FileVersion fileVersion, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.fileVersion = fileVersion;
		this.eventType = eventType;
	}

	public FileVersion getFileVersion() {
		return fileVersion;
	}

	public int getEventType() {
		return eventType;
	}
}
