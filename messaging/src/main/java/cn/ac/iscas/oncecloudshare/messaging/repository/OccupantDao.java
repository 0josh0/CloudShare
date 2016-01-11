package cn.ac.iscas.oncecloudshare.messaging.repository;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;


public interface OccupantDao extends PagingAndSortingRepository<MucOccupant,Long>{

	Page<MucOccupant> findByRoomId(long roomId,Pageable pageable);
	
	Page<MucOccupant> findByUserId(long userId,Pageable pageable);
	
	MucOccupant findByRoomIdAndUserId(long roomId,long userId);
	
	@Query("SELECT o FROM MucRoom r, MucOccupant o "
			+ "WHERE r.id=o.room.id AND o.userId=?1 AND o.readSeq<r.maxSeq")
	Page<MucOccupant> findOccupantsWithUnreadMessages(Long userId,Pageable pageable);
	
	@Modifying
	@Query("UPDATE MucOccupant o SET o.readSeq=?3 WHERE o.room.id=?1 AND o.userId=?2 AND o.readSeq<?3")
	void updateReadSeq(long roomId,long userId,long readSeq);
	
	@Modifying
	@Query("DELETE FROM MucOccupant o WHERE o.room.id=?1 AND o.userId IN ?2")
	void deleteBatch(Long roomId,Set<Long> userIds);
}
