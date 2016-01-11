package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.storageclient;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

public class LocalStorageClient extends StorageClient {

	Path rootPath;

	public LocalStorageClient(String uri) {
		Path path = new Path(uri);
		String rootDir = path.getPath();
		File root = new File(rootDir);
		Preconditions.checkArgument(root.isAbsolute(), rootDir
				+ " is not absolute.");
		try {
			FileUtils.forceMkdir(root);
		} catch (IOException e) {
			throw new IllegalArgumentException("cannot mkdir " + rootDir);
		}
		this.rootPath = path;
	}

	@Override
	protected void connectInternal() throws IOException {
	}

	@Override
	public Path getRootPath() {
		return rootPath;
	}

	@Override
	public ByteSource open(Path path) throws IOException {
		File file = new File(path.toUri().getPath());
		return Files.asByteSource(file);
	}

	@Override
	public ByteSink create(Path path) throws IOException {
		File file = new File(path.toUri().getPath());
		FileUtils.forceMkdir(file.getParentFile());
		return Files.asByteSink(file);
	}

}
