package cn.ac.iscas.oncecloudshare.service.dto;

public class ErrorCode {

	/*
	 * subCode: 01-09 common 10-19 user/dept 20-29 file 30-39 share/link 49-50
	 * admin 50+ extension
	 */

	// 400
	public static final ErrorCode BAD_REQUEST = new ErrorCode(400, 0, "bad request");
	public static final ErrorCode INVALID_PARAM = new ErrorCode(400, 1, "invalid parameter");
	public static final ErrorCode INVALID_SEARCH_QUERY = new ErrorCode(400, 2, "invalid search query");
	public static final ErrorCode INVALID_AVATAR_FILE = new ErrorCode(400, 10, "invalid avatar file");
	public static final ErrorCode PARENT_DEPARTMENT_NOT_EXSISTS = new ErrorCode(400, 15, "parent department id not exists");
	public static final ErrorCode INSUFFICIENT_QUOTA = new ErrorCode(400, 20, "insufficent quota");
	public static final ErrorCode PARENT_NOT_EXISTS = new ErrorCode(400, 21, "parent not exists");
	public static final ErrorCode INVALID_PATH = new ErrorCode(400, 22, "invalid path");
	public static final ErrorCode INVALID_DESTINATION_PATH = new ErrorCode(400, 23, "invaid destination path");
	public static final ErrorCode INVALID_FRAMENT_ID = new ErrorCode(400, 24, "invalid fragment id");
	public static final ErrorCode BATCH_DOWNLOAD_EXCEED_LIMIT = new ErrorCode(400, 25, "exceed batch download limit");
	public static final ErrorCode UNSUPPORTED_EXT = new ErrorCode(400, 40, "unsupported extension");
	public static final ErrorCode INSTALLED_EXT = new ErrorCode(400, 41, "installed extension");

	// 401
	public static final ErrorCode UNAUTHORIZED = new ErrorCode(401, 0, "unauthorized");
	public static final ErrorCode INVALID_TICKET = new ErrorCode(401, 1, "ticket is invalid or has expired");

	// 403
	public static final ErrorCode FORBIDDEN = new ErrorCode(403, 0, "permission denied");
	public static final ErrorCode USER_NOT_ACTIVE = new ErrorCode(403, 1, "user_not_active");
	public static final ErrorCode USER_NOT_APPLYING = new ErrorCode(403, 2);
	public static final ErrorCode WRONG_OLD_PASSWORD = new ErrorCode(403, 10, "wrong old password");
	public static final ErrorCode FILE_NOT_MODIFIABLE = new ErrorCode(403, 20, "file not modifiable");
	public static final ErrorCode FORBIDDEN_FILE_EXTENSION = new ErrorCode(403, 21, "forbdden file extension");
	public static final ErrorCode LINKSHARE_FORBIDDEN = new ErrorCode(403, 30, "禁止操作");
	public static final ErrorCode LINKSHARE_INVALID_PASS = new ErrorCode(403, 31, "提取码错误");
	public static final ErrorCode USERSHARE_FORBIDDEN = new ErrorCode(403, 32, "禁止操作");

	// 404
	public static final ErrorCode NOT_FOUND = new ErrorCode(404, 0, "object not found");
	public static final ErrorCode WRONG_API_URI = new ErrorCode(404, 1, "wrong api uri");
	public static final ErrorCode CONFIG_NOT_FOUND = new ErrorCode(404, 5, "config not found");
	public static final ErrorCode USER_NOT_FOUND = new ErrorCode(404, 10, "user not found");
	public static final ErrorCode AVATAR_NOT_FOUND = new ErrorCode(404, 11, "avatar not found");
	public static final ErrorCode DEPARTMENT_NOT_FOUND = new ErrorCode(404, 15, "department not found");
	public static final ErrorCode FILE_NOT_FOUND = new ErrorCode(404, 20, "file not found");
	public static final ErrorCode FILE_VERSION_NOT_FOUND = new ErrorCode(404, 21, "file version not found");
	public static final ErrorCode MD5_FILE_NOT_FOUND = new ErrorCode(404, 22, "md5 file not found");
	public static final ErrorCode APPLICATION_NOT_FOUND = new ErrorCode(404, 25, "application not found");
	public static final ErrorCode LINKSHARE_NOT_FOUND = new ErrorCode(404, 30, "对应的外链不存在");
	// TODO: 没有个人分享了
	public static final ErrorCode USERSHARE_NOT_FOUND = new ErrorCode(404, 31, "对应的个人分享不存在");	
	public static final ErrorCode FILE_UNFOLLOWED = new ErrorCode(404, 32, "file unfollowed");	
	public static final ErrorCode EXT_NOT_FOUND = new ErrorCode(404, 40, "extension not found");

	// 409
	public static final ErrorCode CONFLICT = new ErrorCode(409, 0, "conflict");
	public static final ErrorCode DUPLICATE_EMAIL = new ErrorCode(409, 10, "duplicate email");
	public static final ErrorCode DUPLICATE_DEPARTMENT_ROUTE = new ErrorCode(409, 15, "duplicate department route");
	public static final ErrorCode DUPLICATE_PATH = new ErrorCode(409, 20, "duplicate path");
	public static final ErrorCode DELETE_LAST_VERSION = new ErrorCode(409, 21, "cannot delete last version");
	public static final ErrorCode DUPLICATE_FILE_FOLLOW = new ErrorCode(409, 32, "file already followed");

	// 410
	public static final ErrorCode APPLICATION_GONE = new ErrorCode(410, 25, "application is gone");
	public static final ErrorCode LINKSHARE_GONE = new ErrorCode(409, 30, "对应的外链已过期");

	// 500
	public static final ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, 0, "internal server error");
	public static final ErrorCode BAD_EXT_FILE = new ErrorCode(500, 40, "bad extension file");
	// 阿里云存储相关
	public static final ErrorCode ALIYUN_ACCESS_ERROR = new ErrorCode(500, 43, "aliyun access error");

	// 503
	public static final ErrorCode SERVICE_UNVAILABLE = new ErrorCode(503, 0, "service unavailable");

	/**
	 * 用户数量已满
	 */
	public static final ErrorCode CREATE_USER_ERROR = new ErrorCode(503, 11, "user count limited");

	public final int statusCode;
	public final int subCode;
	public final String message;

	public ErrorCode(int statusCode, int subCode) {
		this(statusCode, subCode, null);
	}

	public ErrorCode(int statusCode, int subCode, String message) {
		this.statusCode = statusCode;
		this.subCode = subCode;
		this.message = message;
	}

	public int getErrorCode() {
		return statusCode * 100 + subCode;
	}
}
