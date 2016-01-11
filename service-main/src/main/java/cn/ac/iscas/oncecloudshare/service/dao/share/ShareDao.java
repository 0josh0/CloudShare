package cn.ac.iscas.oncecloudshare.service.dao.share;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.share.Share;

public interface ShareDao extends PagingAndSortingRepository<Share, Long>, JpaSpecificationExecutor<Share> {
	@Query("select distinct t1 from Share t1 join t1.recipients t2 where t1.creator.id = ?1 and t2.type = ?2 and t1.status = 'CREATED'")
	Page<Share> findAll(long creatorId, String recipientType, Pageable pageable);

	@Query("select distinct t1 from Share t1 join t1.recipients t2 where t1.creator.id = ?1 and t2.type = ?2 AND t2.identify = ?3 and t1.status = 'CREATED'")
	Page<Share> findAll(Long masterId, String recipientType, Long recipientId, Pageable pageable);
}
