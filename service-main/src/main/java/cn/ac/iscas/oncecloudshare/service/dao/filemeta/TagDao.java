package cn.ac.iscas.oncecloudshare.service.dao.filemeta;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.Tag;

public interface TagDao extends PagingAndSortingRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {
	@Modifying
	@Query("update Tag set orderIndex = ?2 where id = ?1")
	int updateOrder(long id, int order);

	@Modifying
	@Query("update Tag set filesCount = filesCount + (?2) where id = ?1")
	int updateFilesCount(long tagId, long increment);
}
