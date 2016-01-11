package cn.ac.iscas.oncecloudshare.service.service.common;

public final class Configs {

	public static final class Keys {

		public static final String USER_QUOTA = "user.quota";

		public static final String REG_NEED_APPROVAL = "user.registration.approval_enabled";
		public static final String REG_SEND_MAIL = "user.registration.send_mail";

		public static final String FORBIDDEN_EXT = "file.upload.forbidden_ext";

		public static final String MAIL_ACCOUNT = "mail.account";
		public static final String MAIL_PASSWORD = "mail.password";
		public static final String MAIL_PERSONAL = "mail.personal";
		public static final String MAIL_HOST = "mail.host";
		public static final String MAIL_PORT = "mail.port";

		/**
		 * tenant filter 白名单
		 */
		public static final String WEB_TENANT_WHILTE = "tenant.filter.whiteList";

		/**
		 * 审核邮件标题
		 */
		public static final String MAIL_AUDIT_SUBJECT = "mail_template.audit.subject";
		/**
		 * 审核邮件内容
		 */
		public static final String MAIL_AUDIT_CONTENT = "mail_template.audit.content";

		/**
		 * 激活用户web页面url
		 */
		public static final String ACTIVATE_URL = "user.activate.web_url";

		/**
		 * 邮件邀请标题内容
		 */
		public static final String MAIL_INVITATION_SUBJECT = "mail_template.invitation.subject";
		public static final String MAIL_INVITATION_CONTENT = "mail_template.invitation.content";

		public static final String MAIL_TEST_SUBJECT = "mail_template.test.subject";
		public static final String MAIL_TEST_CONTENT = "mail_template.test.content";

		public static final String MAIL_REG_SUBJECT = "mail_template.registration.subject";
		public static final String MAIL_REG_CONTENT = "mail_template.registration.content";
		public static final String MAIL_RESET_PW_SUBJECT = "mail_template.reset_password.subject";
		public static final String MAIL_RESET_PW_CONTENT = "mail_template.reset_password.content";

		public static final String BLOCK_SIZE = "filestorage.block.size";

		public static final String BATCH_DOWNLOAD_NUMBER_LIMIT = "file.batch_download.limit.number";
		public static final String BATCH_DOWNLOAD_SIZE_LIMIT = "file.batch_download.limit.size";

		/**
		 * web端的url地址
		 */
		public static final String CLIENT_WEB_URL = "client.web.url";
		// 是否开启设备审核
		public static final String DEVICE_REVIEW_REQUIRED = "extensions.device.review.required";
		/**
		 * 重置密码web页面url
		 */
		public static final String RESET_PW_URL = "user.reset_password.web_url";

		/**
		 * 插件中心的url
		 */
		public static final String EXT_CENTER_URL = "extension.center.url";
		/**
		 * 插件中心的用户名
		 */
		public static final String EXT_CENTER_USER = "extension.center.user";
		/**
		 * 插件中心的密码
		 */
		public static final String EXT_CENTER_PASS = "extension.center.password";
		/**
		 * 阿里云存储相关
		 */
		public static final String ALIYUN_KEY_ID = "aliyun.key.id";
		public static final String ALIYUN_KEY_SECRET = "aliyun.key.secret";
		public static final String ALIYUN_ENDPOINT = "aliyun.endpoint";
		public static final String ALIYUN_BUCKET_NAME = "aliyun.bucket.name";
		// web端登录"记住我"
		public static final String REMEMBERME_EXPIRE_DAY = "rememberme.expire_day";

		public static final String extensionEnabled(String extName) {
			return "extension." + extName + ".enabled";
		}

		public static final String localStorageRoot(String os) {
			return "filestorage." + os + ".localstorage.root";
		}

		public static final String tempStorageRoot(String os) {
			return "tempstorage." + os + ".root";
		}
	}

	public static final class Defaults {

		public static final long USER_QUOTA = 10 * 1024 * 1024 * 1024L;

		public static final long BLOCK_SIZE = 8 * 1024 * 1024L;
		
		public static final int REMEMBERME_EXPIRE_DAY = 7;
	}
}
