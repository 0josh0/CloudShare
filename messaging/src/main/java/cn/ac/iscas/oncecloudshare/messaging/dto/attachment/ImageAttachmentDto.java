package cn.ac.iscas.oncecloudshare.messaging.dto.attachment;


public class ImageAttachmentDto {

	public final String url;
	public final String thumbnailUrl;
	
	public ImageAttachmentDto(String url, String thumbnailUrl){
		this.url=url;
		this.thumbnailUrl=thumbnailUrl;
	}
	
}
