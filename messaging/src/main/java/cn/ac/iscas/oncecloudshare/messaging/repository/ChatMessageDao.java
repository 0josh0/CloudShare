package cn.ac.iscas.oncecloudshare.messaging.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import cn.ac.iscas.oncecloudshare.messaging.model.chat.ChatMessage;


public interface ChatMessageDao extends PagingAndSortingRepository<ChatMessage,Long>,
	JpaSpecificationExecutor<ChatMessage>{
	
	static final String CONV_CLAUSE=
			" ( ( m.sender=:userId AND m.receiver=:oppositeId ) OR " +
			" ( m.receiver=:userId AND m.sender=:oppositeId ) ) ";
	
	//userId为当前操作用户的id
	static final String DEL_CLAUSE=
			" ( (NOT (:userId=m.sender AND m.senderDel=1)) AND " +
			" (NOT (:userId=m.receiver AND m.receiverDel=1)) )";
	
	@Query("FROM ChatMessage m WHERE "+CONV_CLAUSE+" AND "+DEL_CLAUSE)
	Page<ChatMessage> findByConversation(@Param("userId") long userId,
			@Param("oppositeId") long oppositeId,Pageable pageable);
	
	@Query("FROM ChatMessage m WHERE "+CONV_CLAUSE+" AND "+DEL_CLAUSE
			+ " AND ts >= :begin AND ts <= :end"
			+ " AND content LIKE CONCAT('%',:keyword,'%')")
	Page<ChatMessage> searchByConversation(@Param("keyword") String keyword,
			@Param("begin") long begin, @Param("end") long end,
			@Param("userId") long userId,@Param("oppositeId") long oppositeId,
			Pageable pageable);
	
	@Query("SELECT MAX(seq) FROM ChatMessage m WHERE "+CONV_CLAUSE)
	Long findMaxSeq(@Param("userId") long userId,@Param("oppositeId") long oppositeId);
	
//	@Query(nativeQuery=true,value=
//		"SELECT LEAST(receiver,sender) AS userId, " +
//		"  GREATEST(receiver,sender) AS oppositeId, MAX(seq) AS maxSeq "+
//		"FROM ocs_msg.msg_im "+
//		"GROUP BY userId,oppositeId ")
//	List<Conversation> findAllConvSeq();
	
	@Modifying
	@Query("UPDATE ChatMessage m SET m.receiverDel=1 WHERE m.receiver = ?1 AND m.id IN ?2")
	void deleteBatchByReceiver(long receiver,List<Long> ids);
	
	@Modifying
	@Query("UPDATE ChatMessage m SET m.senderDel=1 WHERE m.sender = ?1 AND m.id IN ?2")
	void deleteBatchBySender(long sender,List<Long> ids);
}
