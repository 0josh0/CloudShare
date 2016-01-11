package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao.FileBlockDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao.StorageDeviceDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.FileBlock;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.security.Ciphers;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.security.Encryptions;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.io.BlockByteSource;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.io.CipherByteSource;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.storageclient.LocalStorageClient;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.storageclient.Path;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.storageclient.StorageClient;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.utils.io.Md5CachingByteSource;

import com.google.common.io.ByteSource;

@Service
@Transactional
public class FileBlockService {
	private static final String ENCRYPT_REQUIRED = "filestorage.advance.encrypt.required";

	@Resource(name="globalConfigService")
	private ConfigService<?> configService;


	@Autowired
	FileBlockDao fbDao;

	@Autowired
	StorageDeviceDao deviceDao;

	@Autowired
	Ciphers ciphers;

	public FileBlock findBlock(String md5) {
		return fbDao.findByMd5(md5);
	}

	private StorageClient getStorageClient(StorageDevice device) {
		String rootDir = device.getDeviceUri();
		return new LocalStorageClient(rootDir);
	}

	private String calculatePath(String md5) {
		String relativePath = md5.substring(0, 2) + "/" + md5.substring(2, 4)
				+ "/" + md5;
		return relativePath;
	}

	public FileBlock saveBlock(ByteSource source, StorageDevice device)
			throws IOException {
		StorageDevice destDevice = device;
		Md5CachingByteSource md5CachingByteSource = Md5CachingByteSource.fromImmutableByteSource(source);
		String md5 = md5CachingByteSource.getMd5();
		FileBlock block = findBlock(md5);
		if (block != null) {
			return block;
		}

		String relativePath = calculatePath(md5);
		
		Encryptions encryption = Encryptions.NULL;
		if (configService.getConfigAsBoolean(ENCRYPT_REQUIRED, false)) {
			source = new CipherByteSource(source,
					ciphers.defaultEncryptCipher());
			encryption = Encryptions.DEFAULT;
		}
		saveBlockContent(source, destDevice, relativePath);

		block = new FileBlock();
		block.setMd5(md5);
		block.setSize(source.size());
		block.setLocation(relativePath);
		block.setStorageDevice(destDevice);
		block.setEncryption(encryption);
		fbDao.save(block);
		return block;
	}

	private URI saveBlockContent(ByteSource source, StorageDevice device,
			String relativePath) throws IOException {
		StorageClient client = getStorageClient(device);
		Path path = new Path(client.getRootPath(), relativePath);
		source.copyTo(client.create(path));
		return path.toUri();
	}

	public BlockByteSource retrieveBlockContent(String md5) throws IOException {
		FileBlock block = findBlock(md5);
		if (block == null) {
			throw new IOException("Block " + md5 + " not exsists");
		}
		if (block.getStorageDevice().getStatus()
				.equals(StorageDevice.DeviceStatus.OFF)) {
			throw new FileNotFoundException("device disabled or removed");
		}
		StorageClient client = getStorageClient(block.getStorageDevice());
		Path path = new Path(client.getRootPath(), block.getLocation());
		ByteSource source = client.open(path);

		// 判断是否加密
		if (!block.getEncryption().equals(Encryptions.NULL)) {
			source = new CipherByteSource(source, ciphers.decryptCipher(block
					.getEncryption()));
		}

		return new BlockByteSource(block, source);
	}
}
