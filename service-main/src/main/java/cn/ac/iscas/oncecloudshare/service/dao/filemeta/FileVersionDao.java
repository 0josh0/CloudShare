package cn.ac.iscas.oncecloudshare.service.dao.filemeta;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;


public interface FileVersionDao extends PagingAndSortingRepository<FileVersion,Long>{

	@Query("FROM FileVersion fv WHERE file.id=?1 AND version=?2")
	FileVersion findByFileAndVersion(long fileId,int version); 

	@Query("FROM FileVersion fv WHERE file.owner.id=?1 AND id in ?2")
	List<FileVersion> findFileVersionsByOwner(long ownerId,List<Long> fileVersionIdList);
}
