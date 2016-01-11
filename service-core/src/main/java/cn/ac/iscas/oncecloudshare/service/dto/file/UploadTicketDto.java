package cn.ac.iscas.oncecloudshare.service.dto.file;


public class UploadTicketDto {

	public String ticket;
	
	/**
	 * 如果不为null，表示可以向该文件夹上传新文件
	 */
	public Long parentId;

	/**
	 * 如果不为null，表示可以上传该文件的新版本
	 */
	public Long fileId;
	
	/**
	 * 多长时间后过期（毫秒数）
	 */
	public long expiresIn;
	
	public UploadTicketDto(){
	}

//	public UploadTicketDto(String ticket, long fileVersionId, long expiresIn){
//		this.ticket=ticket;
//		this.fileVersionId=fileVersionId;
//		this.expiresIn=expiresIn;
//	}
	
	public static UploadTicketDto createForFileUpload(long parentId,String ticket,long expiresIn){
		UploadTicketDto dto=new UploadTicketDto();
		dto.parentId=parentId;
		dto.ticket=ticket;
		dto.expiresIn=expiresIn;
		return dto;
	}
	
	public static UploadTicketDto createForFileVersionUpload(long fileId,String ticket,long expiresIn){
		UploadTicketDto dto=new UploadTicketDto();
		dto.fileId=fileId;
		dto.ticket=ticket;
		dto.expiresIn=expiresIn;
		return dto;
	}
	
	
}
