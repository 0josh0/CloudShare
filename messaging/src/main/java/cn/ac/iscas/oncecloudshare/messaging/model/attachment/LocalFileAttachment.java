package cn.ac.iscas.oncecloudshare.messaging.model.attachment;

import java.io.File;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;


public class LocalFileAttachment implements Attachment {

	private final String key;
	private final File file;
	private final String filename;
	
	public LocalFileAttachment(String key, File file){
		this(key,file,file.getName());
	}
	
	public LocalFileAttachment(String key, File file, String filename){
		this.key=key;
		this.file=file;
		this.filename=filename;
	}

	@Override
	public String getKey(){
		return key;
	}

	@Override
	public String getFilename(){
		return filename;
	}

	@Override
	public ByteSource getContent(){
		return Files.asByteSource(file);
	}

	public File getFile(){
		return file;
	}
}
