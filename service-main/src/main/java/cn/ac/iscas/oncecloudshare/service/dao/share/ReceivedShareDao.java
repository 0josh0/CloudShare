package cn.ac.iscas.oncecloudshare.service.dao.share;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;

public interface ReceivedShareDao extends PagingAndSortingRepository<ReceivedShare, Long>, JpaSpecificationExecutor<ReceivedShare> {
	@Query("select distinct t1 from ReceivedShare t1 join t1.belongsTo t2 where t1.recipient.id = ?1 AND  t2.type = ?2 and t1.share.status = 'CREATED' and t1.isDeleted = false")
	Page<ReceivedShare> findAll(Long recipientId, String recipientType, Pageable pageable);

	@Query("select distinct t1 from ReceivedShare t1 join t1.belongsTo t2 where t1.recipient.id = ?1 AND  t2.type = ?2 AND t2.identify = ?3 and t1.share.status = 'CREATED' and t1.isDeleted = false")
	Page<ReceivedShare> findAll(Long masterId, String recipientType, Long recipientId, Pageable pageable);
}
