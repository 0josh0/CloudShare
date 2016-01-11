package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.io;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;


public class FileByteSource extends MultiBlockByteSource{

	FileSource fileSource; 

	public FileByteSource(FileSource fileSource,List<BlockByteSource> blockSources){
		super(blockSources);
		this.fileSource=fileSource;
	}
	
	public FileSource getFileSource(){
		return fileSource;
	}

	@Override
	public long size() throws IOException{
		return fileSource.getSize();
	}

	@Override
	public ByteSource slice(long offset,long length){
		List<BlockByteSource> inRange=Lists.newArrayList();
		long start=0;
		long counter=0;
		for(BlockByteSource blockSource:blockSources){
			long blockSize=blockSource.getBlock().getSize();
			if(counter+blockSize>offset){
				inRange.add(blockSource);
				if(inRange.size()==1){
					start=offset-counter;
				}
			}
			counter+=blockSize;	
			if(counter>=offset+length){
				break;
			}
		}
		return new MultiBlockByteSource(inRange).slice(start,length);
	}
}
