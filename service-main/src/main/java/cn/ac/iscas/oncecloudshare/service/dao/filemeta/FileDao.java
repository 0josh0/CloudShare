package cn.ac.iscas.oncecloudshare.service.dao.filemeta;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;


public interface FileDao extends PagingAndSortingRepository<File,Long>, JpaSpecificationExecutor<File>{
	
	@Query("FROM File f WHERE owner.id=?1 AND id IN ?2 AND status!='DELETED' ")
	List<File> findMultiFilesByOwner(long ownerId,List<Long> idList);
	
	@Query("FROM File f WHERE parent.id=?1 AND status!='DELETED'")
	Page<File> findByParent(long parentId,Pageable pageable);
	
	@Query("FROM File f WHERE parent.id=?1 AND status='HEALTHY' AND modifiable=TRUE ")
	Page<File> findHealthyChildren(long parentId,Pageable pageable);
	
	@Query("FROM File f WHERE owner.id=?1 AND path=?2 AND status!='DELETED'")
	Page<File> findByOwnerIdAndPath(Long ownerId,String path,Pageable pageable);

	@Query("FROM File f WHERE owner.id=?1 AND path=?2 AND status=?3")
	Page<File> findByOwnerIdAndPathAndStatus(Long ownerId,String path,FileStatus status,Pageable pageable);
	
	@Modifying
	@Query("UPDATE File f SET f.versionSeq=f.versionSeq+1 WHERE f.id=?1")
	int incrVersionSeq(long fileId);
	
	@Modifying
    @Query("UPDATE File f SET f.status = ?2 WHERE f.id = ?1 ")
    int updateStatus(long fileId, FileStatus status);
	
	@Modifying
	@Query(nativeQuery = true, value = "delete from ocs_tag_file where tag_id = ?1")
	int deleteTag(long tagId);
}
