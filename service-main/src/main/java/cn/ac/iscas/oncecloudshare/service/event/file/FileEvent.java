package cn.ac.iscas.oncecloudshare.service.event.file;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

/**
 * 1 移动 2 删除 3 还原 4 彻底删除 5 更新 6 清空回收站 7 创建文件夹
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Interceptable
public class FileEvent extends UserRequestEvent {
	/**
	 * 移动事件
	 */
	public static final int EVENT_MOVE = 1;
	/**
	 * 删除事件
	 */
	public static final int EVENT_TRASH = EVENT_MOVE + 1;
	/**
	 * 还原事件
	 */
	public static final int EVENT_UNTRASH = EVENT_TRASH + 1;
	/**
	 * 上传事件
	 */
	public static final int EVENT_DELETE = EVENT_UNTRASH + 1;
	/**
	 * 更新事件
	 */
	public static final int EVENT_UPDATE = EVENT_DELETE + 1;
	/**
	 * 清空回收站事件
	 */
	public static final int EVENT_CLEAR_TRASH = EVENT_UPDATE + 1;
	/**
	 * 创建文件夹事件
	 */
	public static final int EVENT_MAKE_FOLDER = EVENT_CLEAR_TRASH + 1;

	/**
	 * 重命名事件
	 */
	public static final int EVENT_FILE_RENAME = EVENT_MAKE_FOLDER + 1;

	private final File file;
	private final int eventType;

	private final FileType fileType;

	public enum FileType {
		DIR("#文件夹#"), FILE("#文件#");

		private String type;

		private FileType(String type) {
			this.type = type;
		}

		public String typeName() {
			return type;
		}
	}

	public FileEvent(UserPrincipal principal, File file, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.file = file;

		if (file.getIsDir())
			this.fileType = FileType.DIR;
		else
			this.fileType = FileType.FILE;
		this.eventType = eventType;
	}

	public File getFile() {
		return file;
	}

	public int getEventType() {
		return eventType;
	}

	public FileType getFileType() {
		return fileType;
	}
}
