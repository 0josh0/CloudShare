package cn.ac.iscas.oncecloudshare.service.extensions.device.actions;

import cn.ac.iscas.oncecloudshare.service.model.log.ActionType;
import cn.ac.iscas.oncecloudshare.service.model.log.TargetType;

public enum AdminDeviceAction implements ActionType {
	AGREE("agree", "通过", DeviceTargetType.EXT_DEVICE),
	DISAGREE("disagree", "拒绝", DeviceTargetType.EXT_DEVICE),
	ENABLE("enable", "启用", DeviceTargetType.EXT_DEVICE),
	DISABLE("disable", "禁用", DeviceTargetType.EXT_DEVICE);

	private String code;
	private String name;
	private TargetType target;

	private AdminDeviceAction(String code, String name, TargetType target) {
		this.code = code;
		this.name = name;
		this.target = target;
		target.addActionType(this);
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TargetType getTarget() {
		return target;
	}
}
