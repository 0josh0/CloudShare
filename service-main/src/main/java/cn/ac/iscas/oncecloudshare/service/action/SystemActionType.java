package cn.ac.iscas.oncecloudshare.service.action;

import cn.ac.iscas.oncecloudshare.service.model.log.ActionType;
import cn.ac.iscas.oncecloudshare.service.model.log.TargetType;

public enum SystemActionType implements ActionType {
	/*
	 * 文件操作
	 */
	FILE_UPLOAD("upload", "上传", SystemTargetType.FILE),
	FILE_DOWLOAD("download", "下载", SystemTargetType.FILE),
	FILE_MOVE("move", "移动", SystemTargetType.FILE),
	FILE_TRASH("trash", "删除", SystemTargetType.FILE),
	FILE_UNTRASH("untrash", "还原", SystemTargetType.FILE),
	FILE_DELETE("delete", "彻底删除", SystemTargetType.FILE),
	FILE_UPDATE("update", "更新", SystemTargetType.FILE),
	FILE_CLEARTRASH("clear_trash", "清空回收站", SystemTargetType.FILE),
	FILE_MAKEFOLDER("make_folder", "新建文件夹", SystemTargetType.FILE),
	FILE_RENAME("rename", "重命名", SystemTargetType.FILE),
	;

	private String code;
	private String name;
	private TargetType target;

	private SystemActionType(String code, String name, TargetType target) {
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
