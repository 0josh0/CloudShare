package cn.ac.iscas.oncecloudshare.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucMessage;


public interface MucMessageDao extends PagingAndSortingRepository<MucMessage,Long>{

	Page<MucMessage> findByRoomIdAndTsGreaterThan(long roomId,long ts,Pageable pageable);
	
	@Query("FROM MucMessage m WHERE roomId = :roomId "
			+ " AND ts >= :begin AND ts <= :end "
			+ " AND content LIKE CONCAT('%',:keyword,'%')")
	Page<MucMessage> searchMessagesInRoom(@Param("keyword") String keyword,
			@Param("roomId") long roomId,
			@Param("begin") long begin,@Param("end") long end,
			Pageable pageable); 
	
	@Modifying
	@Query("DELETE FROM MucMessage m WHERE m.roomId=?1")
	void deleteByRoomId(Long roomId);
}
