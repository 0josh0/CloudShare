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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.common.ConfigDao;
import cn.ac.iscas.oncecloudshare.service.model.common.Config;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.AccessMode;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.DataType;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

@Service
@Transactional
public class DefaultConfigService implements ConfigService {

	private static Logger logger = LoggerFactory.getLogger(DefaultConfigService.class);

	@Autowired
	ConfigDao configDao;

	@Resource(name = "defaultConfig")
	private Properties defaultConfig;

	@Override
	public Config find(String key) {
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
					Config config = readFromDefaultConfigFile(key);
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
	private Config readFromDefaultConfigFile(String key) {
		String propValue = defaultConfig.getProperty(key);
		if (propValue == null) {
			return null;
		}
		Iterator<String> it = Splitter.on(",,").trimResults().limit(5).split(propValue).iterator();
		Config config = new Config();
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
		Config config = configDao.findByKey(key);
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
	public Config saveConfig(Config config) {
		configDao.save(config);
		return config;
	}

	@Override
	public Config saveConfig(String key, Object value, boolean addIfAbsent) {
		Preconditions.checkNotNull(value);
		Config config = find(key);

		if (config == null && addIfAbsent) {
			config = new Config();
			config.setKey(key);
			config.setValue(value.toString());
			config.setAdminAccessMode(Config.ADMIN_ACCESS_MODE_DEFAULT_VALUE);
			config.setNormalUserReadable(Config.NORMAL_USER_READABLE_DEFAULT_VALUE);
		}
		if (config != null) {
			config.setValue(value.toString());
			configDao.save(config);
		}
		return config;
	}

	@Override
	public List<Config> findByDomain(String domain) {
		return configDao.findByKeyLike(domain + ".%");
	}

	@Override
	public List<Config> findAll() {
		return (List<Config>) configDao.findAll();
	}
}
