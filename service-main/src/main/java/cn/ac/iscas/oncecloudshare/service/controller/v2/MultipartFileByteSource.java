package cn.ac.iscas.oncecloudshare.service.controller.v2;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.ByteSource;

public class MultipartFileByteSource extends ByteSource {

	MultipartFile file;

	public MultipartFileByteSource(MultipartFile file){
		this.file=file;
	}

	@Override
	public InputStream openStream() throws IOException{
		return file.getInputStream();
	}

	@Override
	public long size() throws IOException{
		return file.getSize();
	}

}