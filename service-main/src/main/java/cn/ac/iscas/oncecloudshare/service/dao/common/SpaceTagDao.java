package cn.ac.iscas.oncecloudshare.service.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.SpaceTag;

public interface SpaceTagDao extends PagingAndSortingRepository<SpaceTag, Long>, JpaSpecificationExecutor<SpaceTag> {
	@Modifying
	@Query("update SpaceTag set orderIndex = ?2 where id = ?1")
	int updateOrder(long id, int order);

	@Modifying
	@Query("update SpaceTag set filesCount = filesCount + (?2) where id = ?1")
	int updateFilesCount(long tagId, long increment);
}
