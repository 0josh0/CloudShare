package cn.ac.iscas.oncecloudshare.service.utils.io;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;

//TODO 完善这个类
public class MultiByteSource extends ByteSource{

	List<ByteSource> sources=Lists.newArrayList();

	public MultiByteSource(List<ByteSource> sources){
		this.sources.addAll(sources);
	}
	
	@Override
	public InputStream openStream() throws IOException{
		return new SequenceInputStream(
				new MultiByteSourceInputStreamEnum(sources));
	}

	@Override
	public long size() throws IOException{
		long size=0;
		for(ByteSource source:sources){
			size+=source.size();
		}
		return size;
	}
	
	@Override
	public ByteSource slice(long offset,long length){
		return new SlicedByteSource(offset,length);
	}

	private final class SlicedByteSource extends ByteSource {

		private final long offset;
		private final long length;

		private SlicedByteSource(long offset, long length){
			checkArgument(offset>=0,"offset (%s) may not be negative",offset);
			checkArgument(length>=0,"length (%s) may not be negative",length);
			this.offset=offset;
			this.length=length;
		}

		@Override
		public InputStream openStream() throws IOException{
			List<ByteSource> inRange=Lists.newArrayList();
			long start=0;
			long counter=0;
			for(ByteSource source: sources){
				long size=source.size();
				if(counter+size>offset){
					inRange.add(source);
					if(inRange.size()==1){
						start=offset-counter;
					}
				}
				counter+=size;
				if(counter>=offset+length){
					break;
				}
			}

			InputStream in=new SequenceInputStream(
					new MultiByteSourceInputStreamEnum(inRange));
			
			if(start>0){
				try{
					ByteStreams.skipFully(in,start);
				}
				catch(Throwable e){
					Closer closer=Closer.create();
					closer.register(in);
					try{
						throw closer.rethrow(e);
					}
					finally{
						closer.close();
					}
				}
			}
			return ByteStreams.limit(in,length);
		}
		
		@Override
		public long size() throws IOException{
			return length;
		}

		@Override
		public ByteSource slice(long offset,long length){
			long maxLength=this.length-offset;
			return super.slice(this.offset+offset,
					Math.min(length,maxLength));
		}

	}

	private class MultiByteSourceInputStreamEnum implements Enumeration<InputStream> {

		List<ByteSource> sources;
		private int index=0;
		
		public MultiByteSourceInputStreamEnum(List<ByteSource> sources){
			this.sources=sources;
		}

		@Override
		public boolean hasMoreElements(){
			return index<sources.size();
		}

		@Override
		public InputStream nextElement(){
			index++;
			try{
				return sources.get(index-1).openStream();
			}
			catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		
	}
}
