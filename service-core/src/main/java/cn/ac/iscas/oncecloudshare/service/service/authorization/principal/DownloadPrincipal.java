package cn.ac.iscas.oncecloudshare.service.service.authorization.principal;

public final class DownloadPrincipal implements Principal {

	private static final long serialVersionUID=1L;

	/**
	 * 可能是普通文件的md5，也可能是临时文件的key
	 */
	public final String key;
	public final String filename;

	public DownloadPrincipal(String key, String filename){
		super();
		this.key=key;
		this.filename=filename;
	}
}
