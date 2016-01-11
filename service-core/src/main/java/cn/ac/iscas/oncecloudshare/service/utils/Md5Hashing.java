package cn.ac.iscas.oncecloudshare.service.utils;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

public class Md5Hashing {

	/**
	 * use ByteSource.hash() instead
	 */
	@Deprecated
	public static HashCode hashStream(final InputStream in) throws IOException{
		return ByteStreams.hash(new InputSupplier<InputStream>(){

			@Override
			public InputStream getInput() throws IOException{
				return in;
			}
		},Hashing.md5());
	}
	
}
