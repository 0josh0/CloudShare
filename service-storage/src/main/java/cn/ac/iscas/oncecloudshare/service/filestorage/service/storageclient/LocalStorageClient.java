package cn.ac.iscas.oncecloudshare.service.filestorage.service.storageclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;


public class LocalStorageClient extends StorageClient{

	Path rootPath;
	
	public LocalStorageClient(String rootDir){
		File root=new File(rootDir);
		Preconditions.checkArgument(root.isAbsolute(),rootDir+" is not absolute.");
		try{
			FileUtils.forceMkdir(root);
		}
		catch(IOException e){
			throw new IllegalArgumentException("cannot mkdir "+rootDir);
		}
		this.rootPath=new Path("file://"+new Path(rootDir).getPath());
	}
	
	@Override
	protected void connectInternal() throws IOException{
	}

	@Override
	public Path getRootPath(){
		return rootPath;
	}

	@Override
	public ByteSource open(Path path) throws IOException{
		File file=new File(path.toUri().getPath());
		return Files.asByteSource(file);
	}

	@Override
	public ByteSink create(Path path) throws IOException{
		File file=new File(path.toUri().getPath());
//		if(file.exists()){
//			throw new IOException(path+" already exists");
//		}
		FileUtils.forceMkdir(file.getParentFile());
		return Files.asByteSink(file);
	}

}
