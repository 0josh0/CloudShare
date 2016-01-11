package cn.ac.iscas.oncecloudshare.service.filestorage.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.filestorage.dao.FileSourceDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.model.BlockAssociation;
import cn.ac.iscas.oncecloudshare.service.filestorage.model.FileBlock;
import cn.ac.iscas.oncecloudshare.service.filestorage.model.FileSourceImpl;
import cn.ac.iscas.oncecloudshare.service.filestorage.service.io.BlockByteSource;
import cn.ac.iscas.oncecloudshare.service.filestorage.service.io.FileByteSource;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.Md5Hashing;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

@Service
@Transactional
public class DefaultFileStorageService implements FileStorageService {
	
	@Autowired
	ConfigService cService;
	
	@Autowired
	FileBlockService fbService;

	@Autowired
	FileSourceDao fsDao;
	
	LockSet<String> md5LockSet=new LockSet<String>();
	
	@Override
	public FileSourceImpl findFileSource(String md5){
		return fsDao.findByMd5(md5);
	}

	@Override
	public FileSourceImpl saveFile(ByteSource byteSource)
			throws IOException{
		String md5=Md5Hashing.hashStream(byteSource.openStream()).toString();
		Lock lock=md5LockSet.getLock(md5);
		lock.lock();
		try{
			//如果文件已存在，直接返回
			FileSourceImpl fileSource=findFileSource(md5);
			if(fileSource!=null){
				return fileSource;
			}
			fileSource=new FileSourceImpl();
			
			long totalSize=byteSource.size();
			long blockSize=cService.getConfigAsLong(Configs.Keys.BLOCK_SIZE,
					Configs.Defaults.BLOCK_SIZE);
			
			//保存block
			List<BlockAssociation> blocks=Lists.newArrayList();
			long processed=0;
			int seq=0;
			while(processed<totalSize){
				ByteSource slice=byteSource.slice(processed,blockSize);
				FileBlock block=fbService.saveBlock(slice);
				BlockAssociation ba=new BlockAssociation(fileSource,block,seq);
				blocks.add(ba);
				
				processed+=blockSize;
				seq++;
			}
			
			//保存元数据
			fileSource.setMd5(md5);
			fileSource.setSize(totalSize);
			fileSource.setBlocks(blocks);
			fsDao.save(fileSource);
			return fileSource;
		}
		finally{
			lock.unlock();
		}
	}

	@Override
	public ByteSource retrieveFileContent(String md5) throws IOException{
		FileSourceImpl fileSource=findFileSource(md5);
		if(fileSource==null){
//			throw new IOException("File "+md5+" not exsists");
			return null;
		}
		List<BlockByteSource> blockSources=Lists.newArrayList();
		for(BlockAssociation ba:fileSource.getBlocks()){
			blockSources.add(fbService.retrieveBlockContent(ba.getFileBlock().getMd5()));
		}
		return new FileByteSource(fileSource,blockSources);
	}
}
