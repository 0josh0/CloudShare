package cn.ac.iscas.oncecloudshare.service.system.service;


public interface ServiceProviderRegistry {

	/**
	 * 获取一个已注册的ServiceProvider
	 * @param clazz
	 * @return
	 */
	<T> T retrieve(Class<? extends ServiceProvider> clazz);
	
	/**
	 * 注册一个ServiceProvider
	 * @param serviceProvider
	 */
	void add(ServiceProvider serviceProvider);

	/**
	 * 注册一个ServiceProvider
	 * @param serviceProvider
	 */
    void add(String serviceProviderFullQualifiedClassname);
}
