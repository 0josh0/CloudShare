package cn.ac.iscas.oncecloudshare.service.extensions.backup.utils;

import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.collect.ImmutableList;

public class Configs {
	private static Configs instance;
	private ConfigService configService;

	private Configs() {
		configService = SpringUtil.getBean(ConfigService.class);
	}

	private static Configs getInstance() {
		synchronized (Configs.class) {
			if (instance == null) {
				instance = new Configs();
			}
		}
		return instance;
	}

	public static String getMysqlBin() {
		return getInstance().configService.getConfig(Key.MYSQL_BIN.code(), null);
	}

	public static String getUserPath() {
		return getInstance().configService.getConfig(Key.USER_PATH.code(), null);
	}

	public static String getUserExpr() {
		return getInstance().configService.getConfig(Key.USER_EXPR.code(), Defaults.USER_EXPR);
	}
	
	public static String getSysPath(){
		return getInstance().configService.getConfig(Key.SYS_PATH.code(), Defaults.SYS_PATH);
	}
	
	public static String getSysExpr(){
		return getInstance().configService.getConfig(Key.SYS_EXPR.code(), Defaults.SYS_EXPR);
	}

	public static String getDbUsername() {
		return getInstance().configService.getConfig(Key.JDBC_USERNAME.code(), null);
	}

	public static String getDbPassword() {
		return getInstance().configService.getConfig(Key.JDBC_PASSWORD.code(), null);
	}

	public static String getDbUrl() {
		return getInstance().configService.getConfig(Key.JDBC_URL.code(), null);
	}

	public static String getMysqlSocket() {
		return getInstance().configService.getConfig(Key.JDBC_SOCKET.code(), null);
	}

	public static ImmutableList<String> keys() {
		ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
		for (Key key : Key.values()) {
			builder.add(key.code());
		}
		return builder.build();
	}

	private static enum Key {
		// mysql路径
		MYSQL_BIN,
		// 用户备份路径
		USER_PATH,
		// 用户备份表达式
		USER_EXPR,
		// 系统备份路径
		SYS_PATH,
		// 系统备份表达式
		SYS_EXPR,
		JDBC_URL,
		JDBC_USERNAME,
		JDBC_PASSWORD,
		JDBC_SOCKET;
		public String code() {
			return "extensions.backup.".concat(name()).replaceAll("_", ".").toLowerCase();
		}
	}

	private static class Defaults {
		public static String USER_EXPR = "0 0 0 * * ?";
		public static String SYS_PATH = "/ocs/bak";
		public static String SYS_EXPR = "0 0 1 * * ?";
	}
}
