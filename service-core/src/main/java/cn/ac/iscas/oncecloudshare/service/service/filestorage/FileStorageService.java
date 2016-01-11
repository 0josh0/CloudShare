package cn.ac.iscas.oncecloudshare.service.service.filestorage;

import java.io.IOException;

import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;

import com.google.common.io.ByteSource;


public interface FileStorageService extends ServiceProvider{

	FileSource findFileSource(String md5);
	
	FileSource saveFile(ByteSource byteSource) throws IOException;

	ByteSource retrieveFileContent(String md5) throws IOException;

}
