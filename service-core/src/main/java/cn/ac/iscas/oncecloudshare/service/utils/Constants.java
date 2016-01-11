package cn.ac.iscas.oncecloudshare.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class Constants {

	public static final String GLOBAL_SCHEMA="cs_global"; 
	public static final String TENANT_SCHEMA_PREFIX="cs_tenant_"; 
	public static final String TENANT_SCHEMA_DEFAULT=TENANT_SCHEMA_PREFIX+"default";
	
	public static final long LOGIN_EXPIRE_TIME=30*DateUtils.MILLIS_PER_MINUTE;
	
	@Value("${msg.secret_key}")
	private String msgSecretKey;
	
	private static Constants instance;
	
	private Constants(){
		instance=this;
	}
	
	public static String getMsgSecretKey(){
		return instance.msgSecretKey;
	}
	
	/**
	 * 操作系统类型
	 * @return windows/linux/mac/other
	 */
	public static String getOsType(){
		String os=System.getProperty("os.name","generic").toLowerCase();
		if(os.indexOf("win")>=0){
			return "windows";
		}
		else if((os.indexOf("mac")>=0) || (os.indexOf("darwin")>=0)){
			return "mac";
		}
		else if(os.indexOf("nux")>=0){
			return "linux";
		}
		return "other";
	}
	
	public static class Company{
		// 域
		public static final String DOMAIN = "company";
	}
	
	public static class BuildInFolders {
		public static final String ROOT = "/";
		public static final String TEMP = "_tmp";
		public static final String BACKUP = "_bak";

		public static final ImmutableList<String> ALL = ImmutableList.of(ROOT, TEMP, BACKUP);
	}
}
