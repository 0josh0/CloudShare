package cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService.ConfigUtil;

@Component("workspace.configs")
public class Configs {
	private static Configs instance;
	
	@Resource(name = "globalConfigService")
	private ConfigService<?> configService;

	public Configs() {
		if (instance == null) {
			instance = this;
		} else {
			throw new RuntimeException("只允许一个Configs的实例！");
		}
	}

	public static String getConfigName(Workspace workspace, String configName) {
		return Constants.DOMAIN + "." + workspace.getId() + "." + configName;
	}

	public static long getDefaultQuota() {
		return instance.configService.getConfigAsLong(Keys.DEFAULT_QUOTA, Defaults.QUOTA);
	}

	public static boolean isDataSourceInitialized() {
		return instance.configService.getConfigAsBoolean(Keys.DATA_INITIALIZED, false);
	}

	public static void setDataSourceInitialized() {
		instance.configService.saveConfig(Keys.DATA_INITIALIZED, true, true);
	}

	public static String getDefaultRole() {
		String role = instance.configService.getConfig(Keys.DEFAULT_ROLE, Defaults.ROLE);
		if (!Roles.has(role)) {
			role = Defaults.ROLE;
		}
		return role;
	}

	protected static String buildKey(String name) {
		return ConfigUtil.buildKey(Constants.DOMAIN, name);
	}

	public static class Keys {
		// 默认配额
		public static final String DEFAULT_QUOTA = buildKey("default_quota");
		// workspace扩展的数据源是否初始化了
		public static final String DATA_INITIALIZED = buildKey("is_data_initialized");
		// workspace默认的角色
		public static final String DEFAULT_ROLE = buildKey("default_role");
		// 上传申请失效时间
		public static final String APPLY_UPLOAD_EXPIRES = buildKey("apply_upload_expires");
	}

	public static class Defaults {
		// 默认配额
		public static final long QUOTA = 10L * 1024 * 1024 * 1024;
		// 默认的角色
		public static final String ROLE = Roles.WRITER;
		// 默认上传申请失效时间30天
		public static final int APPLY_UPLOAD_EXPIRES = 30;
	}
}