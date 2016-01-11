package cn.ac.iscas.oncecloudshare.service.extensions.device.events;

import cn.ac.iscas.oncecloudshare.service.event.UserRequestEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceUser;
import cn.ac.iscas.oncecloudshare.service.service.authorization.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

@Interceptable
public class AdminDeviceEvent extends UserRequestEvent {
	/**
	 * 同意事件
	 */
	public static final int EVENT_AGREED= 1;
	/**
	 * 拒绝事件
	 */
	public static final int EVENT_DISAGREED = EVENT_AGREED + 1;
	/**
	 * 禁用事件
	 */
	public static final int EVENT_ENABLED = EVENT_DISAGREED + 1;
	/**
	 * 禁用事件
	 */
	public static final int EVENT_DISABLED = EVENT_ENABLED + 1;

	private final DeviceUser deviceUser;
	private final int eventType;

	public AdminDeviceEvent(UserPrincipal principal, DeviceUser deviceUser, int eventType) {
		super(SpringUtil.getRequest(), principal);
		this.deviceUser = deviceUser;
		this.eventType = eventType;
	}

	public DeviceUser getDeviceUser() {
		return deviceUser;
	}

	public int getEventType() {
		return eventType;
	}
}