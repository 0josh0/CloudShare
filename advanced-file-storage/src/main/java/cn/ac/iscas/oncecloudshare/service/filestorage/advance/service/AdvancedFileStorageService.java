package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao.FileSourceDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao.StorageDeviceDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.BlockAssociation;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.FileBlock;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.FileSourceImpl;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.io.BlockByteSource;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.io.FileByteSource;
import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.io.Md5CachingByteSource;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

@Service
@Transactional
public class AdvancedFileStorageService implements FileStorageService {

	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	@Autowired
	FileBlockService fbService;

	@Autowired
	FileSourceDao fsDao;

	@Autowired
	StorageDeviceDao deviceDao;

	LockSet<String> md5LockSet = new LockSet<String>();

	@Override
	public FileSource findFileSource(String md5) {
		return fsDao.findByMd5(md5);
	}

	@Override
	public FileSource saveFile(ByteSource byteSource) throws IOException {
		Md5CachingByteSource md5CachingByteSource = Md5CachingByteSource
				.fromImmutableByteSource(byteSource);
		String md5 = md5CachingByteSource.getMd5();
		Lock lock = md5LockSet.getLock(md5);
		lock.lock();
		try {
			// 如果文件已存在，直接返回
			FileSourceImpl fileSource = fsDao.findByMd5(md5);
			if (fileSource != null) {
				return fileSource;
			}
			fileSource = new FileSourceImpl();

			long totalSize = byteSource.size();
			long blockSize = configService.getConfigAsLong(Configs.Keys.BLOCK_SIZE,
					Configs.Defaults.BLOCK_SIZE);
			// 保存block
			StorageDevice device = determinActiveDevice(); // 将文件块保存在哪个目录下
			List<BlockAssociation> blocks = Lists.newArrayList();
			long processed = 0;
			int seq = 0;
			while (processed < totalSize) {
				ByteSource slice = byteSource.slice(processed, blockSize);
				FileBlock block = fbService.saveBlock(slice, device);
				BlockAssociation ba = new BlockAssociation(fileSource, block,
						seq);
				blocks.add(ba);

				processed += blockSize;
				seq++;
			}

			// 保存元数据
			fileSource.setMd5(md5);
			fileSource.setSize(totalSize);
			fileSource.setBlocks(blocks);
			fsDao.save(fileSource);
			return fileSource;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ByteSource retrieveFileContent(String md5) throws IOException {
		FileSourceImpl fileSource = fsDao.findByMd5(md5);
		if (fileSource == null) {
			return null;
		}
		List<BlockByteSource> blockSources = Lists.newArrayList();
		for (BlockAssociation ba : fileSource.getBlocks()) {
			blockSources.add(fbService.retrieveBlockContent(ba.getFileBlock()
					.getMd5()));
		}
		return new FileByteSource(fileSource, blockSources);
	}

	/**
	 * 获取用于存储文件的设备对象
	 * 
	 * @return
	 */
	private StorageDevice determinActiveDevice() throws IOException {
		List<StorageDevice> devices = deviceDao
				.findByStatus(StorageDevice.DeviceStatus.ACTIVE);
		if (devices != null && devices.size() > 0) {
			return devices.get(0);
		} else {
			throw new IOException("no active devices found");
		}
	}
}
