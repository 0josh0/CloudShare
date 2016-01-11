package cn.ac.iscas.oncecloudshare.service.service.multitenancy;

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

import cn.ac.iscas.oncecloudshare.service.dao.multitenancy.GlobalConfigDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.common.Config;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.AccessMode;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.DataType;
import cn.ac.iscas.oncecloudshare.service.model.common.GlobalConfig;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

@Service(value="globalConfigService")
@Transactional
public class DefaultGlobalConfigService implements ConfigService<GlobalConfig> {

	private static Logger logger = LoggerFactory.getLogger(DefaultGlobalConfigService.class);

	@Autowired
	GlobalConfigDao globalConfigDao;

	@Resource(name = "defaultGlobalConfig")
	private Properties defaultConfig;

	@Override
	public GlobalConfig find(String key) {
		return globalConfigDao.findByKey(key);
	}

	/**
	 * 启动时载入默认配置
	 */
	@PostConstruct
	public void loadDefault() {
		Set<String> keys = ImmutableSet.copyOf(globalConfigDao.findAllKeys());
		Enumeration<Object> enumeration = defaultConfig.keys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement().toString();
			if (keys.contains(key) == false) {
				try {
					GlobalConfig config = readFromDefaultConfigFile(key);
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
	private GlobalConfig readFromDefaultConfigFile(String key) {
		String propValue = defaultConfig.getProperty(key);
		if (propValue == null) {
			return null;
		}
		Iterator<String> it = Splitter.on(",,").trimResults().limit(5).split(propValue).iterator();
		GlobalConfig config = new GlobalConfig();
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
		GlobalConfig config = globalConfigDao.findByKey(key);
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
	public GlobalConfig saveConfig(GlobalConfig config) {
		globalConfigDao.save((GlobalConfig)config);
		return config;
	}

	@Override
	public GlobalConfig saveConfig(String key, Object value, boolean addIfAbsent) {
		Preconditions.checkNotNull(value);
		GlobalConfig config = find(key);

		if (config == null && addIfAbsent) {
			config = new GlobalConfig();
			config.setKey(key);
			config.setValue(value.toString());
			config.setAdminAccessMode(Config.ADMIN_ACCESS_MODE_DEFAULT_VALUE);
			config.setNormalUserReadable(Config.NORMAL_USER_READABLE_DEFAULT_VALUE);
		}
		if (config != null) {
			config.setValue(value.toString());
			globalConfigDao.save((GlobalConfig)config);
		}
		return config;
	}

	@Override
	public List<GlobalConfig> findByDomain(String domain) {
		return globalConfigDao.findByKeyLike(domain + ".%");
	}

	@Override
	public List<GlobalConfig> findAll() {
		return  (List<GlobalConfig>) globalConfigDao.findAll();
	}

	@Override
	public Page<GlobalConfig> search(List<SearchFilter> searchFilters, Pageable pageable) {
		try {
			Specification<GlobalConfig> spec = Specifications.fromFilters(searchFilters, GlobalConfig.class);
			return globalConfigDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}

	@Override
	public List<GlobalConfig> search(List<SearchFilter> filters) {
		try {
			Specification<GlobalConfig> spec = Specifications.fromFilters(filters, GlobalConfig.class);
			return globalConfigDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}
}
