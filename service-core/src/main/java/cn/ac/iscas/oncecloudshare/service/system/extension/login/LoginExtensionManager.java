package cn.ac.iscas.oncecloudshare.service.system.extension.login;

@Deprecated
public interface LoginExtensionManager {

	public void loadLoginExtension(String name,LoginExtension extension);
	
	public void unloadLoaginExtension(String name);
	
	public LoginExtension getExtension(String name);
	
	public LoginExtension getDefaultExtension();
	
}
