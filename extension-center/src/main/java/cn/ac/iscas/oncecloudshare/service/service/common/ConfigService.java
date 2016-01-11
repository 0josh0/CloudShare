package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.model.common.Config;

/**
 * 系统配置服务
 * 
 * @author Chen Hao
 */
public interface ConfigService {

	public Config find(String key);

	public <T> T getConfig(String key, T defaultValue);

	public List<Config> findByDomain(String domain);

	public List<Config> findAll();

	public Config saveConfig(Config config);

	public Config saveConfig(String key, Object value, boolean addIfAbsent);
}
