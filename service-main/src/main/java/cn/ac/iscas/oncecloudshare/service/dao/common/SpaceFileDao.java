package cn.ac.iscas.oncecloudshare.service.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;

@Repository
public interface SpaceFileDao extends PagingAndSortingRepository<SpaceFile, Long>, JpaSpecificationExecutor<SpaceFile> {
	@Query("FROM SpaceFile f WHERE owner.id=?1 AND id IN ?2 AND status!='DELETED' ")
	List<SpaceFile> findMultiFilesByOwner(long ownerId, List<Long> idList);

	@Modifying
	@Query("UPDATE SpaceFile f SET f.versionSeq=f.versionSeq+1 WHERE f.id=?1")
	int incrVersionSeq(long fileId);

	@Modifying
	@Query("UPDATE SpaceFile f SET f.follows=f.follows+(?2) WHERE f.id=?1")
	int incrFollows(long fileId, long increment);
	
	@Modifying
	@Query("UPDATE SpaceFile f SET f.downloads=f.downloads+(?2) WHERE f.id=?1")
	int incrDownloads(long fileId, long increment);
	
	@Modifying
	@Query(nativeQuery = true, value = "delete from ocs_space_tag_file where tag_id = ?1")
	int deleteTag(long tagId);
}