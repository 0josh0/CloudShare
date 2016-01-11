package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.common.ConfigDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.AccessMode;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.DataType;
import cn.ac.iscas.oncecloudshare.service.model.common.TenantConfig;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

@Service(value = "tenantConfigService")
@Transactional
public class DefaultTenantConfigService implements ConfigService<TenantConfig> {

	private static Logger logger = LoggerFactory.getLogger(DefaultTenantConfigService.class);

	@Autowired
	ConfigDao configDao;

	@Resource(name = "defaultConfig")
	private Properties defaultConfig;

	@Override
	public TenantConfig find(String key) {
		return configDao.findByKey(key);
	}

	/**
	 * 启动时载入默认配置
	 */
	@PostConstruct
	public void loadDefault() {
		Set<String> keys = ImmutableSet.copyOf(configDao.findAllKeys());
		Enumeration<Object> enumeration = defaultConfig.keys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement().toString();
			if (keys.contains(key) == false) {
				try {
					TenantConfig config = readFromDefaultConfigFile(key);
					saveConfig(config);
				} catch (Exception e) {
					logger.error("error loading default config: " + key, e);
				}
			}
		}
		defaultConfig = null;
	}

	/**
	 * 尝试读取default-config.properties文件
	 * 
	 * @param key
	 * @return
	 */
	private TenantConfig readFromDefaultConfigFile(String key) {
		String propValue = defaultConfig.getProperty(key);
		if (propValue == null) {
			return null;
		}
		Iterator<String> it = Splitter.on(",,").trimResults().limit(5).split(propValue).iterator();
		TenantConfig config = new TenantConfig();
		config.setKey(key);
		config.setValue(it.next());
		if (it.hasNext()) {
			config.setDataType(DataType.valueOf(it.next()));
		}
		if (it.hasNext()) {
			config.setAdminAccessMode(AccessMode.valueOf(it.next()));
		}
		if (it.hasNext()) {
			config.setNormalUserReadable(Boolean.valueOf(it.next()));
		}
		if (it.hasNext()) {
			config.setDescription(it.next());
		}
		return config;
	}

	/**
	 * 读取系统配置
	 */
	@Override
	public String getConfig(String key, String defaultValue) {
		TenantConfig config = configDao.findByKey(key);
		return config == null ? defaultValue : config.getValue();
	}

	@Override
	public Integer getConfigAsInteger(String key, Integer defaultValue) {
		Integer value = Ints.tryParse(getConfig(key, ""));
		return value == null ? defaultValue : value;
	}

	@Override
	public Long getConfigAsLong(String key, Long defaultValue) {
		Long value = Longs.tryParse(getConfig(key, ""));
		return value == null ? defaultValue : value;
	}

	@Override
	public Boolean getConfigAsBoolean(String key, Boolean defaultValue) {
		String strValue = getConfig(key, "").toLowerCase();
		if (strValue.equals("false")) {
			return false;
		} else if (strValue.equals("true")) {
			return true;
		}
		return defaultValue;
	}

	@Override
	public TenantConfig saveConfig(TenantConfig config) {
		configDao.save(config);
		return config;
	}

	@Override
	public TenantConfig saveConfig(String key, Object value, boolean addIfAbsent) {
		Preconditions.checkNotNull(value);
		TenantConfig config = find(key);

		if (config == null && addIfAbsent) {
			config = new TenantConfig();
			config.setKey(key);
			config.setValue(value.toString());
			config.setAdminAccessMode(TenantConfig.ADMIN_ACCESS_MODE_DEFAULT_VALUE);
			config.setNormalUserReadable(TenantConfig.NORMAL_USER_READABLE_DEFAULT_VALUE);
		}
		if (config != null) {
			config.setValue(value.toString());
			configDao.save(config);
		}
		return config;
	}

	@Override
	public List<TenantConfig> findByDomain(String domain) {
		return configDao.findByKeyLike(domain + ".%");
	}

	@Override
	public List<TenantConfig> findAll() {
		return (List<TenantConfig>) configDao.findAll();
	}

	@Override
	public Page<TenantConfig> search(List<SearchFilter> searchFilters, Pageable pageable) {
		try {
			Specification<TenantConfig> spec = Specifications.fromFilters(searchFilters, TenantConfig.class);
			return configDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}

	@Override
	public List<TenantConfig> search(List<SearchFilter> filters) {
		try {
			Specification<TenantConfig> spec = Specifications.fromFilters(filters, TenantConfig.class);
			return configDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}
}
