package cn.ac.iscas.oncecloudshare.messaging.service.attachment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;

import cn.ac.iscas.oncecloudshare.messaging.model.attachment.LocalFileAttachment;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;


public class FileAttachmentService implements AttachmentService {
	
	public static final int RANDOM_FILENAME_LENGTH=16;
	
	private static final SimpleDateFormat YEAR_DIR_FORMAT=
			new SimpleDateFormat("yyyy");
	private static final SimpleDateFormat MONTH_DIR_FORMAT=
			new SimpleDateFormat("yyyy/MM");
	private static final SimpleDateFormat DAY_DIR_FORMAT=
			new SimpleDateFormat("yyyy/MM/dd");
	
	protected final File baseDir;
	protected final long sizeLimit;
	protected final Set<String> acceptableExtensions;
	
	
	public FileAttachmentService(String baseDirPath, long sizeLimit,
			Set<String> acceptableExtensions){
		super();
		this.baseDir=new File(baseDirPath);
		try{
			FileUtils.forceMkdir(baseDir);
		}
		catch(IOException e){
			throw new RuntimeException("cannot mkdir: "+baseDirPath,e);
		}
		this.sizeLimit=sizeLimit;
		this.acceptableExtensions=ImmutableSet.copyOf(Iterables.transform(
				acceptableExtensions,
				new Function<String,String>(){

						@Override
						public String apply(String input){
							return input.toLowerCase();
						}
					}
				)
		);
	}

	@Override
	public long getSizeLimit(){
		return sizeLimit;
	}

	@Override
	public boolean acceptFileExtension(String extension){
		return acceptableExtensions.contains(extension.toLowerCase());
	}
	
	protected String generateRandomFilename(String extension){
		return RandomStringUtils.randomAlphanumeric(RANDOM_FILENAME_LENGTH)
				+"."+extension;
	}
	
	protected String getRelativePath(String filename){
		return DAY_DIR_FORMAT.format(new Date())+"/"+filename;
	}
	
	protected LocalFileAttachment saveAttachmentToLocalFile(ByteSource content,
			String filename) throws IOException{
		String relativePath=getRelativePath(filename);
		File file=new File(baseDir,relativePath);
		FileUtils.forceMkdir(file.getParentFile());
		
		content.copyTo(Files.asByteSink(file));
		return new LocalFileAttachment(relativePath,file,filename);
	}

	@Override
	public LocalFileAttachment saveAttachment(ByteSource content,String extension) throws IOException{
		Preconditions.checkArgument(acceptFileExtension(extension),
				"unacceptable file extension: "+extension);
		Preconditions.checkArgument(content.size()<=getSizeLimit(),
				"cannot accept content larger than "+getSizeLimit());
		
		String filename=generateRandomFilename(extension);
		return saveAttachmentToLocalFile(content,filename);
	}

	@Override
	public LocalFileAttachment getAttachment(String key){
		File file=new File(baseDir,key);
		if(file.exists()==false){
			return null;
		}
		return new LocalFileAttachment(key,file);
	}
	
	/**
	 * 删除某天之前的文件
	 * @param date
	 * @throws IOException 
	 */
	protected void deleteFileBeforeSpecifiedDay(Date date) throws IOException{
		Date expiredDate=new Date(date.getTime());
		while(true){
			File dir=new File(baseDir,DAY_DIR_FORMAT.format(date));
			if(dir.exists()==false){
				break;
			}
			FileUtils.forceDelete(dir);
			expiredDate=DateUtils.addDays(expiredDate,-1);
		}
	}
}
