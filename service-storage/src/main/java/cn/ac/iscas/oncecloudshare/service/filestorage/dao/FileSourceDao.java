package cn.ac.iscas.oncecloudshare.service.filestorage.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.filestorage.model.FileSourceImpl;

public interface FileSourceDao extends
		PagingAndSortingRepository<FileSourceImpl,Long> {

	FileSourceImpl findByMd5(String md5);
}
