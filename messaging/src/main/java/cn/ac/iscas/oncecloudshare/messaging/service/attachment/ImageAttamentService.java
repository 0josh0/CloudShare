package cn.ac.iscas.oncecloudshare.messaging.service.attachment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.messaging.model.attachment.LocalFileAttachment;
import cn.ac.iscas.oncecloudshare.messaging.utils.Constants;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;

@Service
public class ImageAttamentService extends FileAttachmentService {
	
	private static Logger logger=LoggerFactory.getLogger(ImageAttamentService.class);

	public static final long SIZE_LIMIT=5*1024*1024;
	public static final Set<String> ACCEPTABLE_EXTENSIONS=
			ImmutableSet.of("jpg","jpeg","png","gif");
	
	private static final int THUMBNAIL_SIZE=150;
	
	@Autowired
	public ImageAttamentService(
			@Value(value="${attachment.storage_path}")
			String attachmentStoragePath){
		super(attachmentStoragePath+"/"+"image",
				SIZE_LIMIT,ACCEPTABLE_EXTENSIONS);
	}
	
	private String getThumbnailFilename(String originalFilename){
		int idx=originalFilename.lastIndexOf('.');
		if(idx<0){
			return originalFilename+"_thumbnail";
		}
		else{
			return originalFilename.substring(0,idx)+"_thumbnail"
					+originalFilename.substring(idx);
		}
	}
	
	private File getThumbnailFile(LocalFileAttachment originalAttachment){
		return new File(originalAttachment.getFile().getParent(),
				getThumbnailFilename(originalAttachment.getFilename()));
	}
	
	private void saveThumbnail(LocalFileAttachment originalAttachment)
			throws IOException{
		BufferedImage oriImage=ImageIO.read(originalAttachment.getFile());
		BufferedImage thumbnail=Scalr.resize(oriImage,THUMBNAIL_SIZE);
		File thumbnailFile=getThumbnailFile(originalAttachment);
		String ext=FilenameUtils.getExtension(thumbnailFile.getName());
		ImageIO.write(thumbnail,ext,thumbnailFile);
	}
	
	@Override
	public LocalFileAttachment saveAttachment(ByteSource content,
			String extension) throws IOException{
		LocalFileAttachment originalAttachment=
				super.saveAttachment(content,extension);
		saveThumbnail(originalAttachment);
		return originalAttachment;
	}
	
	public LocalFileAttachment getThumbnailAttachment(String key){
		LocalFileAttachment originalAttachment=getAttachment(key);
		if(originalAttachment==null){
			return null;
		}
		return new LocalFileAttachment(key,getThumbnailFile(originalAttachment));
	}
	
	/**
	 * 每天凌晨3点清除过期文件
	 */
	@Scheduled(cron="0 0 3 * * *")
	public void cleanUpExpiredFiles(){
		int expirationDays=Constants.attachmentExpirationDays();
		if(expirationDays>0){
			logger.info("clean up expired image files before {} days",expirationDays);
			try{
				deleteFileBeforeSpecifiedDay(DateUtils.addDays(new Date(),-1-expirationDays));
			}
			catch(IOException e){
				logger.error("error cleaning up expired image files:",e);
			}
		}
	}
}
