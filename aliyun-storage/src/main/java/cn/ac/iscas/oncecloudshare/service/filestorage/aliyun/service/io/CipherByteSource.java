package cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.service.io;

import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.google.common.io.ByteSource;

public class CipherByteSource extends ByteSource {
	private ByteSource source;
	private Cipher cipher;

	public CipherByteSource(ByteSource source, Cipher cipher) {
		this.source = source;
		this.cipher = cipher;
	}

	@Override
	public InputStream openStream() throws IOException {
		CipherInputStream cipherIn = new CipherInputStream(
				this.source.openStream(), cipher);
		return cipherIn;
	}

}
