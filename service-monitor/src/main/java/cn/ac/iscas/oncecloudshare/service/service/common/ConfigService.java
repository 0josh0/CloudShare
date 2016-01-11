package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.model.common.Config;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.DataType;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * 系统配置服务
 * 
 * @author Chen Hao
 */
public interface ConfigService {
	
	public Config find(String key);
	
	public String getConfig(String key,String defaultValue);
	
	public Integer getConfigAsInteger(String key,Integer defaultValue);
	
	public Long getConfigAsLong(String key,Long defaultValue);
	
	public Boolean getConfigAsBoolean(String key,Boolean defaultValue);
	
	public List<Config> findByDomain(String domain);
	
	public List<Config> findAll();
	
	public Config saveConfig(Config config);
	
	public Config saveConfig(String key,Object value,boolean addIfAbsent);
	
	public static final class ConfigUtil{
		
		public static String buildKey(String domain,String subKey){
			Preconditions.checkNotNull(domain);
			Preconditions.checkNotNull(subKey);
			domain=domain.replaceAll("\\.","_");
			return domain+"."+subKey;
		}
		
		public static Object parseConfigValue(String value,DataType dataType){
			Preconditions.checkNotNull(value);
			switch(dataType){
			case BOOLEAN:
				if(value.equalsIgnoreCase("true") ||
						value.equalsIgnoreCase("false")){
					return Boolean.valueOf(value);
				}
				return null;
			case INT:
				return Ints.tryParse(value);
			case LONG:
				return Longs.tryParse(value);
			case STRING:
				return value;
			default:
				break;
			}
			return null;
		}
	}
}
