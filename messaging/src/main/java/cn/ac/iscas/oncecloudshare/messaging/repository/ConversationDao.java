package cn.ac.iscas.oncecloudshare.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import cn.ac.iscas.oncecloudshare.messaging.model.chat.Conversation;




public interface ConversationDao extends PagingAndSortingRepository<Conversation,Long>{

	static final String CONV_CLAUSE="( c.userId = :userId AND c.oppositeId = :oppositeId )";
	
	static final String USER_CLAUSE=
			" ( ( c.userId = :user1 AND c.oppositeId = :user2 ) OR " +
			" ( c.userId = :user2 AND c.oppositeId = :user1 ) ) ";
	
	Conversation findByUserIdAndOppositeId(long userId,long oppositeId);
	
	Page<Conversation> findByUserId(Long userId,Pageable pageable);
	
	@Query("SELECT c FROM Conversation c WHERE c.userId=?1 AND c.readSeq<c.maxSeq")
	Page<Conversation> findConvsWithUnreadMessages(Long userId,Pageable pageable);
	
	@Modifying
	@Query("UPDATE Conversation c SET c.readSeq = :seq"
			+ " WHERE ( c.readSeq < :seq AND c.maxSeq >= :seq ) AND "+CONV_CLAUSE)
	int updateReadSeq(@Param("userId") long userId,
			@Param("oppositeId") long oppositeId,@Param("seq") long seq);

	
	@Modifying
	@Query("UPDATE Conversation c SET c.maxSeq=c.maxSeq+1,c.updateTime=now() WHERE "+USER_CLAUSE)
	void incrMaxSeq(@Param("user1") long user1,@Param("user2") long user2);
}
