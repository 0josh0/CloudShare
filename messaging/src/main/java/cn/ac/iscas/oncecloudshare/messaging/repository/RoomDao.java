package cn.ac.iscas.oncecloudshare.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucRoom;


public interface RoomDao extends PagingAndSortingRepository<MucRoom,Long>{

	Page<MucRoom> findByOwner(Long ownerId,Pageable pageable);
	
	@Query("SELECT r FROM MucRoom r, MucOccupant o WHERE r.id=o.room.id AND o.userId=?1")
	Page<MucRoom> findByUser(Long userId,Pageable pageable);
	
//	@Query("SELECT r FROM MucRoom r, MucOccupant o "
//			+ "WHERE r.id=o.room.id AND o.userId=?1 AND o.readSeq<r.maxSeq")
//	Page<MucRoom> findRoomsWithUnreadMessages(Long userId,Pageable pageable);
	
	@Modifying
	@Query("DELETE MucRoom r WHERE r.id=?1 AND r.owner=?2")
	void safeDelete(Long roomId,Long userId);
	
	
}
