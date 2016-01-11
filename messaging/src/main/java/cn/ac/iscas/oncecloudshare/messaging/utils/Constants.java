package cn.ac.iscas.oncecloudshare.messaging.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {

	public static final String TENANT_SCHEMA_DEFAULT="cs_msg_tenant_default";

	public static final String TENANT_SCHEMA_PREFIX="cs_msg_tenant_";

	private static Constants instance;
	
	public static Constants getInstance(){
		return instance;
	}
	
	@Value(value="${domain}")
	String domain;
	
	@Value(value="${notif.username}")
	String notifUsername;
	
	@Value(value="${notif.password}")
	String notifPassword;
	
	@Value(value="${rest.server}")
	String restServerAddr;
	
	@Value(value="${attachment.expiration_days}")
	int attachmentExpirationDays;
	
	@Value(value="${rest.msg_secret_key}")
	String msgSecretKey;
	
	private Constants(){
		instance=this;
	}
	
	
	public static String domain(){
		return instance.domain;
	}
	
	public static String mucSubdomain(){
		return "chat."+instance.domain;
	}
	
//	@Deprecated
//	public static long notifUserId(){
//		return 0L;
//	}
	
	public static String notifUsername(){
		return instance.notifUsername;
	}
	
	public static String notifPassword(){
		return instance.notifPassword;
	}
	
	public static String restServerAddr(){
		return instance.restServerAddr;
	}
	
	public static int attachmentExpirationDays(){
		return instance.attachmentExpirationDays;
	}
	
	public static String secretKey(){
		return instance.msgSecretKey;
	}
}
