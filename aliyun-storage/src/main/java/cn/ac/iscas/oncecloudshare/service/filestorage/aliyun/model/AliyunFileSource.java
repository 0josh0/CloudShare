package cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.conn.EofSensorInputStream;

import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.OSSObject;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;

/**
 * 文件存储信息
 */
public class AliyunFileSource extends ByteSource implements FileSource {

	public static class UserMatas {
		// 加密方式
		public static final String ENCRYPTION = "encription";
		// 文件大小
		public static final String FILE_SIZE = "fileSize";
	}

	private OSSClient ossClient;
	private OSSObject object;

	public AliyunFileSource(OSSClient ossClient, OSSObject object) {
		Preconditions.checkNotNull(object);
		this.ossClient = ossClient;
		this.object = object;
	}

	public static AliyunFileSource create(OSSClient client, String bucket, String md5, ByteSource byteSource) throws IOException {
		ObjectMetadata meta = new ObjectMetadata();
		meta.addUserMetadata(UserMatas.FILE_SIZE, String.valueOf(byteSource.size()));
		meta.setContentLength(byteSource.size());
		InputStream in = byteSource.openStream();
		try {
			client.putObject(bucket, md5, in, meta);
			return new AliyunFileSource(client, client.getObject(bucket, md5));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public String getMd5() {
		return object.getKey();
	}

	public Long getSize() {
		long fileSize = NumberUtils.toLong(getUserMeta(UserMatas.FILE_SIZE), -1);
		return fileSize > 0 ? fileSize : object.getObjectMetadata().getContentLength();
	}

	@Override
	public long size() throws IOException {
		return getSize();
	}

	protected String getUserMeta(String key) {
		Preconditions.checkNotNull(key);
		return object.getObjectMetadata().getUserMetadata().get(key);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getRowMata(String key) {
		Preconditions.checkNotNull(key);
		return (T) object.getObjectMetadata().getRawMetadata().get(key);
	}

	@Override
	public InputStream openStream() throws IOException {
		InputStream stream = object.getObjectContent();
		try{
			stream.available();
		} catch(IOException e){
			object = ossClient.getObject(object.getBucketName(), object.getKey());
			stream = object.getObjectContent();
		}
		return stream;
	}
}
