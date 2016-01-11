package cn.ac.iscas.oncecloudshare.service.service.common;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import cn.ac.iscas.oncecloudshare.service.model.common.TempItem;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

@Service
public class TempFileStorageService {
	
	private static Logger logger=LoggerFactory.getLogger(TempFileStorageService.class);
	
	private static final String TEMP_ITEM_KEY="temp_file";
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	
	@Autowired
	TempItemService tiService;
	
	private File getRoot(){
		String rootDir=configService.getConfig(
				Configs.Keys.tempStorageRoot(Constants.getOsType()),"");
		File file=new File(rootDir);
		try{
			FileUtils.forceMkdir(file);
		}
		catch(IOException e){
			throw new RuntimeException("cannot make temp file dir: "+rootDir);
		}
		return file;
	}
	
	/**
	 * 保存一个临时文件
	 * @param byteSource
	 * @param expiresIn
	 * @return 这个临时文件的key
	 * @throws IOException
	 */
	public String saveTempFile(ByteSource byteSource,long expiresIn) throws IOException{
		Preconditions.checkArgument(expiresIn>0);
		TempItem ti=tiService.save(TEMP_ITEM_KEY,"",expiresIn);
		File file=new File(getRoot(),ti.getKey());
		byteSource.copyTo(Files.asByteSink(file));
		return ti.getKey();
	}
	
	public ByteSource getTempFile(String key){
		TempItem ti=tiService.find(key);
		if(ti==null){
			return null;
		}
		File file=new File(getRoot(),ti.getKey());
		if(!file.exists()){
			return null;
		}
		return Files.asByteSource(file);
	}
	
	/**
	 * 每天凌晨3点清除过期文件
	 */
	@Scheduled(cron="0 0 3 * * *")
	public void clearExpiredFile(){
		File root=getRoot();
		for(String filename:root.list()){
			if(tiService.find(filename)==null){
				try{
					FileUtils.forceDelete(new File(root,filename));
				}
				catch(IOException e){
					logger.warn("error deleting expried temp file {} : {}",
							filename,e);
				}
			}
		}
	}
}
