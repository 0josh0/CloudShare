package cn.ac.iscas.oncecloudshare.messaging.service.attachment;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.messaging.utils.Constants;

import com.google.common.collect.ImmutableSet;

@Service
public class AudioAttamentService extends FileAttachmentService {
	
	private static Logger logger=LoggerFactory.getLogger(AudioAttamentService.class);

	public static final long SIZE_LIMIT=5*1024*1024;
	public static final Set<String> ACCEPTABLE_EXTENSIONS=
			ImmutableSet.of("mp3","amr");
	
	@Autowired
	public AudioAttamentService(
			@Value(value="${attachment.storage_path}")
			String attachmentStoragePath){
		super(attachmentStoragePath+"/"+"audio",
				SIZE_LIMIT,ACCEPTABLE_EXTENSIONS);
	}
	
	/**
	 * 每天凌晨3点清除过期文件
	 */
	@Scheduled(cron="0 0 3 * * *")
	public void cleanUpExpiredFiles(){
		int expirationDays=Constants.attachmentExpirationDays();
		if(expirationDays>0){
			logger.info("clean up expired audio files before {} days",expirationDays);
			try{
				deleteFileBeforeSpecifiedDay(DateUtils.addDays(new Date(),-1-expirationDays));
			}
			catch(IOException e){
				logger.error("error cleaning up expired audio files:",e);
			}
		}
	}
}
