package cn.ac.iscas.oncecloudshare.service.filestorage.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.filestorage.model.FileBlock;


public interface FileBlockDao extends PagingAndSortingRepository<FileBlock,Long>{

	FileBlock findByMd5(String md5);
}
