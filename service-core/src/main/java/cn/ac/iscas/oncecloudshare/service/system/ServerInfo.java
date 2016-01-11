package cn.ac.iscas.oncecloudshare.service.system;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;

@Component
public class ServerInfo {
	@Resource(name = "globalConfigService")
	private ConfigService<?> configService;

	private static ServerInfo instance;

	public static class Keys {
		// 版本名称
		public static final String VERSION_NAME = "sys.version.name";
		// 版本密码
		public static final String VERSION_NUMBER = "sys.version.number";
		// 内部版本号
		public static final String BUILD_NUMBER = "sys.build.number";
	}

	public static class Defaults {
		// 版本名称
		public static final String VERSION_NAME = "undefined";
		// 版本密码
		public static final String VERSION_NUMBER = VERSION_NAME;
		// 内部版本号
		public static final String BUILD_NUMBER = VERSION_NAME;
	}

	public ServerInfo() {
		Preconditions.checkArgument(instance == null);
		instance = this;
	}

	/**
	 * 获取内部版本号
	 * 
	 * @return
	 */
	public static String getBuildNumber() {
		return instance.configService.getConfig(Keys.BUILD_NUMBER, Defaults.BUILD_NUMBER);
	}

	/**
	 * 获取版本名称
	 * 
	 * @return
	 */
	public static String getVersionName() {
		return instance.configService.getConfig(Keys.VERSION_NAME, Defaults.VERSION_NAME);
	}

	public static String getVersionNumber() {
		return instance.configService.getConfig(Keys.VERSION_NUMBER, Defaults.VERSION_NUMBER);
	}
}
