package cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class Roles {
	private static final Logger _logger = LoggerFactory.getLogger(Roles.class);

	public static final String OWNER = "owner";
	public static final String ADMIN = "admin";
	public static final String WRITER = "writer";
	public static final String LIMITED_WRITER = "limited_writer";
	public static final String READER = "reader";
	public static final String SEPARATED = "separated";
	public static final String USER = "user";
	public static final String ANON = "anon";

	public static final ImmutableList<String> ALL = ImmutableList.of(OWNER, ADMIN, WRITER, LIMITED_WRITER, SEPARATED, READER, USER, ANON);

	public static int compare(String role1, String role2) {
		int index1 = ALL.indexOf(role1);
		int index2 = ALL.indexOf(role2);
		if (index1 == -1 || index2 == -1) {
			_logger.warn("错误的角色名：{}，{}", role1, role2);
			return index1 - index2;
		}
		return index2 - index1;
	}

	public static boolean has(String role) {
		return ALL.indexOf(role) > -1;
	}
	
	public static String getDisplayName(String role){
		if (ADMIN.equals(role)){
			return "管理员";
		}
		if (WRITER.equals(role)){
			return "贡献者";
		}
		if (LIMITED_WRITER.equals(role)){
			return "贡献者(受限)";
		}
		if (READER.equals(role)){
			return "阅读者";
		}
		if (SEPARATED.equals(role)){
			return "仅可见自己";
		}
		if (OWNER.equals(role)){
			return "群主";
		}
		return null;
	}
}
