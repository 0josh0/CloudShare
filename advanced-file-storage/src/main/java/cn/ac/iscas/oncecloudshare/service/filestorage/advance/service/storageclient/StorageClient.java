package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.storageclient;

import java.io.IOException;
import java.net.URI;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;

public abstract class StorageClient {
	
	StorageClient get(URI uri) throws IOException{
		
		return null;
	}
	
	boolean connected=false;
	
	public void connect() throws IOException{
		if(!connected){
			connectInternal();
			connected=true;
		}
	}
	
	protected abstract void connectInternal() throws IOException;
	
	public abstract Path getRootPath();
	
	public abstract ByteSource open(Path path) throws IOException;
	
	public abstract ByteSink create(Path path) throws IOException;
}
