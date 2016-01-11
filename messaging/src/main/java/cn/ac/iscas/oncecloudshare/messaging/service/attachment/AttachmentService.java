package cn.ac.iscas.oncecloudshare.messaging.service.attachment;

import java.io.IOException;

import com.google.common.io.ByteSource;

import cn.ac.iscas.oncecloudshare.messaging.model.attachment.Attachment;


public interface AttachmentService {

	/**
	 * 最大支持的文件大小
	 * @return
	 */
	public long getSizeLimit();
	
	/**
	 * 是否接受该格式的文件
	 * @param extension
	 * @return
	 */
	public boolean acceptFileExtension(String extension);
	
	/**
	 * 保存附件
	 * @param content
	 * @param specifiedFilename 指定文件名，如果为null，将产生随机的文件名
	 * @return
	 * @throws IOException 
	 */
	public Attachment saveAttachment(ByteSource content,String extension) throws IOException;
	
	/**
	 * 获取附件
	 * @param key
	 * @return
	 */
	public Attachment getAttachment(String key);
}
