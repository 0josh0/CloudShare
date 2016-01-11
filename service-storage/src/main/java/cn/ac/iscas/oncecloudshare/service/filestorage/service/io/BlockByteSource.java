package cn.ac.iscas.oncecloudshare.service.filestorage.service.io;

import java.io.IOException;
import java.io.InputStream;

import cn.ac.iscas.oncecloudshare.service.filestorage.model.FileBlock;

import com.google.common.io.ByteSource;


public class BlockByteSource extends ByteSource{
	
	/**
	 * block meta
	 */
	FileBlock block;

	/**
	 * the real source;
	 */
	ByteSource source;

	public BlockByteSource(FileBlock block, ByteSource source){
		this.block=block;
		this.source=source;
	}

	public FileBlock getBlock(){
		return block;
	}
	
	@Override
	public long size() throws IOException{
		return block.getSize();
	}
	
	@Override
	public InputStream openStream() throws IOException{
		return source.openStream();
	}

	
}
