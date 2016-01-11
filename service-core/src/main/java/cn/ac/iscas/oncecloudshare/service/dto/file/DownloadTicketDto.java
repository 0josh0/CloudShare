package cn.ac.iscas.oncecloudshare.service.dto.file;



public final class DownloadTicketDto {

	public final String ticket;
	
	/**
	 * 多长时间后过期（毫秒数）
	 */
	public final long expiresIn;
	

	public DownloadTicketDto(String ticket, long expiresIn){
		this.ticket=ticket;
		this.expiresIn=expiresIn;
	}
	
	
}
