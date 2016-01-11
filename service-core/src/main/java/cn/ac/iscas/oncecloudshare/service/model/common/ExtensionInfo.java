package cn.ac.iscas.oncecloudshare.service.model.common;

public class ExtensionInfo {
	// TODO:扩展的编号，该编号是扩展中心的编号？
	public Long id;
	public String name;
	public String version;
	public String description;
	// 最小支持的Service端版本
	public String minSupport;
	// 最大支持的Service端版本
	public String maxSupport;
	public boolean enabled;
	// 是否已经安装
	public boolean installed;
	// 是否支持
	public Boolean supported;
}
