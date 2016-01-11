package cn.ac.iscas.oncecloudshare.service.filestorage.service;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.filestorage.dao.FileBlockDao;
import cn.ac.iscas.oncecloudshare.service.filestorage.model.FileBlock;
import cn.ac.iscas.oncecloudshare.service.filestorage.service.io.BlockByteSource;
import cn.ac.iscas.oncecloudshare.service.filestorage.service.storageclient.LocalStorageClient;
import cn.ac.iscas.oncecloudshare.service.filestorage.service.storageclient.Path;
import cn.ac.iscas.oncecloudshare.service.filestorage.service.storageclient.StorageClient;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.utils.Md5Hashing;

import com.google.common.io.ByteSource;

@Service
public class FileBlockService {
	
	@Autowired
	ConfigService cService;

	@Autowired
	FileBlockDao fbDao;
	
	public FileBlock findBlock(String md5){
		return fbDao.findByMd5(md5);
	}
	
	private StorageClient getStorageClient(){
		String rootDir=cService.getConfig(
				Configs.Keys.localStorageRoot(Constants.getOsType()),"");
		return new LocalStorageClient(rootDir);
	}
	
	private Path calculatePath(Path root,String md5){
		String relativePath=md5.substring(0,2)+"/"+md5.substring(2,4)+"/"+md5;
		return new Path(root,relativePath);
	}
	
	public FileBlock saveBlock(ByteSource source) throws IOException{
		String md5=Md5Hashing.hashStream(source.openStream()).toString();
		FileBlock block=findBlock(md5);
		if(block!=null){
			return block;
		}
		URI uri=saveBlockContent(md5,source);
		
		block=new FileBlock();
		block.setMd5(md5);
		block.setSize(source.size());
		block.setLocation(uri.toString());
		fbDao.save(block);
		return block;
	}
	
	private URI saveBlockContent(String md5,ByteSource source) throws IOException{
		StorageClient client=getStorageClient();
		Path path=calculatePath(client.getRootPath(),md5);
		source.copyTo(client.create(path));
		return path.toUri();
	}
	
	public BlockByteSource retrieveBlockContent(String md5) throws IOException{
		FileBlock block=findBlock(md5);
		if(block==null){
			throw new IOException("Block "+md5+" not exsists");
		}
		StorageClient client=getStorageClient();
		Path path=calculatePath(client.getRootPath(),md5);
		return new BlockByteSource(block,client.open(path));
	}
}
