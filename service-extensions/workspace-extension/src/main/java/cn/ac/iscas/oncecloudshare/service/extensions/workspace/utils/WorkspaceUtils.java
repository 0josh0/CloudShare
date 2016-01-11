package cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;

import com.google.common.collect.ImmutableList;

public class WorkspaceUtils {
	@SuppressWarnings("unused")
	private static Logger _logger = LoggerFactory.getLogger(WorkspaceUtils.class);
	// 工作空间域
	public static final String DOMAIN = "extension.workspace";
	/**
	 * 匿名用户的用户id
	 */
	public static final long ANON_USERID = Long.MIN_VALUE;

	public static class PrincipalConfigKey {
		public static final String MEMBER_INFO = "member_info";
	}

	public static class Status {
		// 申请中
		public static final String APPLY = "apply";
		// 活跃中
		public static final String ACITVE = "active";
		// 申请被拒绝
		public static final String REFUSED = "refused";
		// 申请被取消
		public static final String CANCELED = "canceled";
		// 被解散的
		public static final String DISMISSED = "dismissed";
	}

	public static class ErrorCodes {
		public static ErrorCode WORKSPACE_NOT_FOUND = new ErrorCode(404, 50, "找不到对应的工作空间");
		public static ErrorCode APPLICATION_NOT_FOUND = new ErrorCode(404, 51, "找不到对应的工作空间申请");
		public static ErrorCode TEAMMATE_NOT_FOUND = new ErrorCode(404, 52, "找不到对应的成员");
		public static ErrorCode FILE_NOT_FOUND = new ErrorCode(404, 53, "找不到对应的文件");
		// 重复加入
		public static ErrorCode DUPLICATE_JOIN = new ErrorCode(409, 50, "You have already joined the workspace");

		public static ErrorCode FILE_EXPECTED = new ErrorCode(400, 50, "file expected");
		public static ErrorCode FOLDER_EXPECTED = new ErrorCode(400, 51, "folder expected");
	}

	public static class BuildInFolders {
		public static final String ROOT = "/";
		public static final String TEMP = "_tmp";
		public static final String BACKUP = "_bak";

		public static final ImmutableList<String> ALL = ImmutableList.of(ROOT, TEMP, BACKUP);
	}

	public static class ApplicationTypes {
		public static final String CREATE_WORKSPACE = "create_workspace";
		public static final String JOIN = "join";
		public static final String UPLOAD = "upload";
		public static final String UPLOAD_VERSION = "upload_version";
	}
}