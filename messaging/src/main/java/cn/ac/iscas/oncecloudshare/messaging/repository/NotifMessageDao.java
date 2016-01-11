package cn.ac.iscas.oncecloudshare.messaging.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;


public interface NotifMessageDao extends PagingAndSortingRepository<NotifMessage,Long>,
	JpaSpecificationExecutor<NotifMessage>{

	@Query("FROM NotifMessage m where m.receiver=:receiver AND m.del=0")
	Page<NotifMessage> findByReceiver(@Param("receiver") Long receiver,
			Pageable pageable);
	
//	@Query("FROM NotifMessage m where m.receiver=:receiver AND m.readFlag=:readFlag AND m.ts<=:endTs AND m.del=0")
//	Page<NotifMessage> findByReceiverAndReadFlag(@Param("receiver") Long receiver,
//			@Param("readFlag") boolean readFlag,@Param("endTs") long endTs,Pageable pageable);
	
//	@Query("SELECT count(m) FROM NotifMessage m WHERE m.receiver=?1 AND m.readFlag=0")
//	Long countUnread(Long receiver);
	
	@Modifying
	@Query("UPDATE NotifMessage m SET m.readFlag=1 WHERE m.receiver=?1 AND m.id in ?2")
	int markAsReadBatch(Long receiver,List<Long> ids);
	
	@Modifying
	@Query("UPDATE NotifMessage m SET m.del=1 WHERE m.receiver=?1 AND m.id in ?2")
	int deleteBatch(Long receiver,List<Long> ids);
}
