package cn.ac.iscas.oncecloudshare.service.filestorage.advance.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.FileBlock;


public interface FileBlockDao extends PagingAndSortingRepository<FileBlock,Long>{

	FileBlock findByMd5(String md5);
}
