package cn.ac.iscas.oncecloudshare.service.filestorage.service.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;


public class MultiBlockByteSource extends ByteSource{

	List<BlockByteSource> blockSources=Lists.newArrayList();

	public MultiBlockByteSource(List<BlockByteSource> blockSources){
		this.blockSources.addAll(blockSources);
	}

	@Override
	public long size() throws IOException{
		long size=0;
		for(BlockByteSource blockSource:blockSources){
			size+=blockSource.size();
		}
		return size;
	}

	@Override
	public InputStream openStream() throws IOException{
		return new SequenceInputStream(new BlockStreamEnum());
	}

	private class BlockStreamEnum implements Enumeration<InputStream>{

		int index=0;
		
		@Override
		public boolean hasMoreElements(){
			return index<blockSources.size();
		}

		@Override
		public InputStream nextElement(){
			index++;
			try{
				return blockSources.get(index-1).openStream();
			}
			catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		
	}
}
