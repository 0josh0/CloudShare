package cn.ac.iscas.oncecloudshare.guava.io;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Preconditions;
import com.google.common.io.InputSupplier;

public class MultiPartFiles {
	public static InputSupplier<InputStream> newInputStreamSupplier(final MultipartFile file) {
		Preconditions.checkNotNull(file);
		return new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return file.getInputStream();
			}
		};
	}
}
