package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.ac.iscas.oncecloudshare.service.model.common.Config;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.DataType;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * 配置服务
 * 
 * @author One
 */
public interface ConfigService<T extends Config> {

	public T find(String key);

	public String getConfig(String key, String defaultValue);

	public Integer getConfigAsInteger(String key, Integer defaultValue);

	public Long getConfigAsLong(String key, Long defaultValue);

	public Boolean getConfigAsBoolean(String key, Boolean defaultValue);

	public List<T> findByDomain(String domain);

	public List<T> findAll();

	public T saveConfig(T config);

	public T saveConfig(String key, Object value, boolean addIfAbsent);

	public Page<T> search(List<SearchFilter> searchFilters, Pageable pageable);

	public List<T> search(List<SearchFilter> filters);

	public static final class ConfigUtil {

		public static String buildKey(String domain, String subKey) {
			Preconditions.checkNotNull(domain);
			Preconditions.checkNotNull(subKey);
			domain = domain.replaceAll("\\.", "_");
			return domain + "." + subKey;
		}

		public static Object parseConfigValue(String value, DataType dataType) {
			Preconditions.checkNotNull(value);
			switch (dataType) {
			case BOOLEAN:
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
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
