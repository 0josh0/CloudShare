package cn.ac.iscas.oncecloudshare.service.model.filemeta;

/**
 * 文件状态
 * 
 * @author Chen Hao
 */
public enum FileStatus{
	
	/**
	 * 正常状态
	 */
	HEALTHY,
	
	/**
	 * 回收站
	 */
	TRASHED,
	
	/**
	 * 祖先在回收站
	 */
	ANCESTOR_TRASHED,
	
	/**
	 * 已删除
	 */
	DELETED,

}