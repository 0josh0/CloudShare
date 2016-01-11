package cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Permissions {
	public static class WorkSpace {
		public static final String VIEW = "workspace:view";
		public static final String DOWNLOAD = "workspace:download";
		public static final String EDIT = "workspace:edit";
		public static final String UPLOAD = "workspace:upload";
		public static final String LIMITED_UPLOAD = "workspace:limitedUpload";
		public static final String FOLLOW = "workspace:follow";// 收藏
		public static final List<String> ALL = ImmutableList.of(VIEW, DOWNLOAD, EDIT, UPLOAD, FOLLOW);
	}

	public static class Member {
		public static final String EDIT = "edit";
		public static final String CHANGE_ROLE = "changeRole";
		public static final String KICK = "kick";
		public static final List<String> ALL = ImmutableList.of(EDIT, CHANGE_ROLE, KICK);

		public static final String domainPermission(String permission) {
			return "workspaceMember:".concat(permission);
		}

		public static final List<String> domainPermissions() {
			List<String> results = Lists.newArrayList();
			for (String permission : ALL) {
				results.add(domainPermission(permission));
			}
			return results;
		}
	}

	public static final Function<String, String> removeDomain = new Function<String, String>() {
		@Override
		public String apply(String input) {
			int index = input.lastIndexOf(":");
			return index == -1 ? input : input.substring(index + 1);
		}
	};
}
