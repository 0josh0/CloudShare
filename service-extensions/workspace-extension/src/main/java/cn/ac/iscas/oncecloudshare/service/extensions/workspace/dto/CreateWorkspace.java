package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.AccessModifier;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Configs;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;

public class CreateWorkspace {
	/**
	 * 空间名称
	 */
	@NotNull
	@Length(min = 1, max = 32)
	private String name;
	/**
	 * 开启文件审核，默认不开启
	 */
	private boolean openFileReview = false;
	/**
	 * 访问权限。默认为protected
	 */
	private AccessModifier accessModifier = AccessModifier.PROTECTED;
	/**
	 * 成员默认角色。默认为阅读者
	 */
	private String defaultRole = Roles.READER;
	/**
	 * 空间配额
	 */
	private long quota = Configs.getDefaultQuota();
	/**
	 * 空间描述
	 */
	@Length(max = 255)
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOpenFileReview() {
		return openFileReview;
	}

	public void setOpenFileReview(boolean openFileReview) {
		this.openFileReview = openFileReview;
	}

	public AccessModifier getAccessModifier() {
		return accessModifier;
	}

	public void setAccessModifier(AccessModifier accessModifier) {
		this.accessModifier = accessModifier;
	}

	public String getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(String defaultRole) {
		this.defaultRole = defaultRole;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}
}
