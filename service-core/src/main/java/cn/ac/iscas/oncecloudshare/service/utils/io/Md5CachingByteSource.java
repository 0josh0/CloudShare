package cn.ac.iscas.oncecloudshare.service.utils.io;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

/**
 * 缓存ByteSource的md5值
 *
 * @author Chen Hao
 */
public class Md5CachingByteSource extends ByteSource{

	private ByteSource source;
	
	private String md5=null;
	
	private Md5CachingByteSource(ByteSource source){
		this.source=source;
	}
	
	public static Md5CachingByteSource fromImmutableByteSource(ByteSource source){
		if(source instanceof Md5CachingByteSource){
			return (Md5CachingByteSource)source;
		}
		return new Md5CachingByteSource(source);
	}
	
	public String getMd5() throws IOException{
		if(md5==null){
			if(source instanceof ContentMd5Computed){
				md5=((ContentMd5Computed)source).getMd5();
			}
			else{
				md5=hash(Hashing.md5()).toString();
			}
		}
		return md5;
	}

	@Override
	public InputStream openStream() throws IOException{
		return source.openStream();
	}

	@Override
	public long size() throws IOException{
		return source.size();
	}
	
	@Override
	public ByteSource slice(long offset,long length){
		return source.slice(offset,length);
	}
}
