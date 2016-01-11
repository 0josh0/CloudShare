package cn.ac.iscas.oncecloudshare.service.filestorage.advance.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;

import com.google.common.base.Strings;

public class FileUtils {
	/**
	 * 获取存储设备的总容量
	 * 
	 * @param uriStr
	 * @return
	 */
	public static Long getTotalSpace(String uriStr) {
		URI uri = checkURI(uriStr);
		StorageDevice.DeviceType schema = StorageDevice.DeviceType.of(uri
				.getScheme());
		if (schema == null) {
			return Long.valueOf(0);
		}
		switch (schema) {
		case FILE:
			File path = new File(uri.getPath());
			forceMkdir(path);
			return path.getTotalSpace();
		default:
			return Long.valueOf(0);
		}
	}

	/**
	 * 获取存储设备的剩余容量
	 * 
	 * @param uriStr
	 * @return
	 */
	public static Long getFreeSpace(String uriStr) {
		URI uri = checkURI(uriStr);
		StorageDevice.DeviceType schema = StorageDevice.DeviceType.of(uri
				.getScheme());
		if (schema == null) {
			return Long.valueOf(0);
		}
		switch (schema) {
		case FILE:
			File path = new File(uri.getPath());
			forceMkdir(path);
			return path.getFreeSpace();
		default:
			return Long.valueOf(0);
		}
	}

	private static URI checkURI(String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			throw new IllegalArgumentException("invalid uri string");
		}
		URI tmpUri = null;
		try {
			tmpUri = new URI(uri);
			return tmpUri;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("format of uri not correct");
		}
	}

	private static void forceMkdir(File dir) {
		try {
			org.apache.commons.io.FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			throw new IllegalArgumentException("cannot mkdir " + dir);
		}
	}

	public static void main(String[] args) {
		System.out.println(getTotalSpace("file:///C:/cloudshare"));
		System.out.println(getFreeSpace("file:///C:/cloudshare"));
	}
}
