package cn.ac.iscas.oncecloudshare.service.extensions.company.space.events;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.service.shiro.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

/**
 * 1 移动 2 删除 3 还原 4 彻底删除 5 更新 6 清空回收站 7 创建文件夹 8 收藏 9 取消收藏
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Interceptable
public class SpaceFileEvent extends UserRequestEvent {
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
	 * 创建文件夹事件
	 */
	public static final int EVENT_FLLOW = EVENT_MAKE_FOLDER + 1;
	/**
	 * 创建文件夹事件
	 */
	public static final int EVENT_UNFLLOW = EVENT_FLLOW + 1;

	private final BaseSpace space;
	private final SpaceFile file;
	private final int eventType;

	public SpaceFileEvent(UserPrincipal principal, BaseSpace space, SpaceFile file, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.space = space;
		this.file = file;
		this.eventType = eventType;
	}

	public BaseSpace getSpace() {
		return space;
	}

	public SpaceFile getFile() {
		return file;
	}

	public int getEventType() {
		return eventType;
	}
}
