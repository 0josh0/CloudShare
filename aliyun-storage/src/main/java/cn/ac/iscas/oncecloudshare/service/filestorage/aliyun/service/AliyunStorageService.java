package cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.service;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.exceptions.AccessException;
import cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.model.AliyunFileSource;
import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.io.Md5CachingByteSource;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSErrorCode;
import com.aliyun.openservices.oss.OSSException;
import com.aliyun.openservices.oss.model.OSSObject;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;

@Service
public class AliyunStorageService implements FileStorageService {
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	private OSSClient ossClient;

	private String bucketName;

	LockSet<String> md5LockSet = new LockSet<String>();

//	@SuppressWarnings("deprecation")
	public OSSClient getOSSClient() {
		refreshOSSClient();
//		if (ossClient == null) {
//			refreshOSSClient();
//			if (!ossClient.doesBucketExist(bucketName)){
//				ossClient.createBucket(bucketName);
//			}
//		}
		return ossClient;
	}

	public void refreshOSSClient() {
		String key = configService.getConfig(Configs.Keys.ALIYUN_KEY_ID, "k7qmrMT4nqPSkpCs");
		String secret = configService.getConfig(Configs.Keys.ALIYUN_KEY_SECRET, "wOnD5aDknA7TBgdUruW2xWjJmCVgSE");
		String endpoint = configService.getConfig(Configs.Keys.ALIYUN_ENDPOINT, "http://oss-cn-qingdao.aliyuncs.com");
		bucketName = configService.getConfig(Configs.Keys.ALIYUN_BUCKET_NAME, "mtenants");
		if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(secret)) {
			throw new AccessException();
		}
		ossClient = new OSSClient(endpoint, key, secret);
	}

	@Override
	public AliyunFileSource findFileSource(String md5) {
		OSSObject object =  getOSSObject(md5);
		if (object == null) {
			return null;
		}
		return new AliyunFileSource(ossClient, object);
	}

	@Override
	public FileSource saveFile(ByteSource byteSource) throws IOException {
		Md5CachingByteSource md5CachingByteSource = Md5CachingByteSource.fromImmutableByteSource(byteSource);
		String md5 = md5CachingByteSource.getMd5();
		Lock lock = md5LockSet.getLock(md5);
		lock.lock();
		try {
			OSSObject object = getOSSObject(md5);
			if (object != null) {
				return new AliyunFileSource(ossClient, object);
			}
			return AliyunFileSource.create(getOSSClient(), bucketName, md5, byteSource);
		}
		catch(Exception e){
			throw new IOException(e);
		}
		finally {
			lock.unlock();
		}
	}
	
	private OSSObject getOSSObject(String md5){
		try{
			return getOSSClient().getObject(bucketName, md5);
		} catch(OSSException e){
			if (OSSErrorCode.NO_SUCH_KEY.equals(e.getErrorCode())){
				return null;
			}
			throw e;
		}
	}

	@Override
	public ByteSource retrieveFileContent(String md5) throws IOException {
		AliyunFileSource source = findFileSource(md5);
		return source;
	}
}
