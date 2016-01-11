package cn.ac.iscas.oncecloudshare.messaging.dto;


public enum BasicErrorCode implements ErrorCode{
	
	/*
	subCode:
	01-09 common
	10-19 user/dept
	20-29 file
	30-39 share/link
	49-50 admin
	50+   extension
	 */
	
	//400
	BAD_REQUEST(400,0,"bad request"),
	INVALID_PARAM(400,1,"invalid parameter"),
	INVALID_SEARCH_QUERY(400,2,"invalid search query"),
	INVALID_AVATAR_FILE(400,10,"invalid avatar file"),
	PARENT_DEPARTMENT_NOT_EXSISTS(400,15,"parent department id not exists"),
	INSUFFICIENT_QUOTA(400,20,"insufficent quota"),
	PARENT_NOT_EXISTS(400,21,"parent not exists"),
	INVALID_PATH(400,22,"invalid path"),
	INVALID_DESTINATION_PATH(400,23,"invaid destination path"),
	INVALID_FRAMENT_ID(400,24,"invalid fragment id"),
	
	//401
	UNAUTHORIZED(401,0,"unauthorized"),
	INVALID_TICKET(401,1,"ticket is invalid or has expired"),
	
	//403
	FORBIDDEN(403,0,"permission denied"),
	WRONG_OLD_PASSWORD(403,10,"wrong old password"),
	FILE_NOT_MODIFIABLE(403,20,"file not modifiable"),
	FORBIDDEN_FILE_EXTENSION(403,21,"forbdden file extension"),
	LINKSHARE_FORBIDDEN(403,30, "禁止操作"),
	LINKSHARE_INVALID_PASS(403,31, "提取码错误"),
	USERSHARE_FORBIDDEN(403, 32, "禁止操作"),
	
	//404
	NOT_FOUND(404,0,"object not found"),
	WRONG_API_URI(404,1,"wrong api uri"),
	CONFIG_NOT_FOUND(404,5,"config not found"),
	USER_NOT_FOUND(404,10,"user not found"),
	AVATAR_NOT_FOUND(404,11,"avatar not found"),
	DEPARTMENT_NOT_FOUND(404,15,"department not found"),
	FILE_NOT_FOUND(404,20,"file not found"),
	FILE_VERSION_NOT_FOUND(404,21,"file version not found"),
	MD5_FILE_NOT_FOUND(404,22,"md5 file not found"),
	LINKSHARE_NOT_FOUND(404, 30, "对应的外链不存在"),
	USERSHARE_NOT_FOUND(404,31, "对应的个人分享不存在"),
	EXT_NOT_FOUND(404,40,"extension not found"),
	
	//409
	CONFLICT(409,0,"conflict"),
	DUPLICATE_EMAIL(409,10,"duplicate email"),
	DUPLICATE_DEPARTMENT_ROUTE(409,15,"duplicate department route"),
	DUPLICATE_PATH(409,20,"duplicate path"),
	DELETE_LAST_VERSION(409,21,"cannot delete last version"),
	
	// 410
	LINKSHARE_GONE(409,30,"对应的外链已过期"),
	
	//500
	INTERNAL_SERVER_ERROR(500,0,"internal server error"),
	
	// 503
	SERVICE_UNVAILABLE(503,0,"service unavailable"),
	;
	

	public final int statusCode;
	public final int subCode;
	public final String message;
	
	BasicErrorCode(int statusCode, int subCode){
		this(statusCode,subCode,null);
	}
	
	BasicErrorCode(int statusCode, int subCode, String message){
		this.statusCode=statusCode;
		this.subCode=subCode;
		this.message=message;
	}
	
	@Override
	public int getStatusCode(){
		return statusCode;
	}
	
	@Override
	public int getErrorCode(){
		return statusCode*100+subCode;
	}
	
	@Override
	public String getMessage(){
		return message;
	}
	
}
