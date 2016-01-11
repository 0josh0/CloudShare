package cn.ac.iscas.oncecloudshare.messaging.service.chat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.messaging.dto.chat.ChatMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.chat.UnreadConversationDigest;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.ChatMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.Conversation;
import cn.ac.iscas.oncecloudshare.messaging.repository.ChatMessageDao;
import cn.ac.iscas.oncecloudshare.messaging.repository.ConversationDao;
import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.Specifications;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service
@Transactional
public class ChatMessageService extends BaseService{

	@Autowired
	ChatMessageDao cmDao;
	
	@Autowired
	ConversationDao cDao;
	
	public Page<ChatMessage> findByConversation(long userId,
			long oppositeId,Pageable pageable){
		return cmDao.findByConversation(userId,oppositeId,pageable);
	}
	
	public ChatMessage findLastMessage(long userId,long oppositeId){
		PageRequest pageRequest=new PageRequest(0,1,
				new Sort(Direction.DESC,"createTime"));
		Page<ChatMessage> page=cmDao.findByConversation(userId,oppositeId,
				pageRequest);
		return Iterables.getFirst(page,null);
	}
	
	public Page<ChatMessage> searchByConversation(String keyword,
			long begin, long end,
			long userId,long oppositeId,
			Pageable pageable){
		return cmDao.searchByConversation(keyword,begin,end,
				userId,oppositeId,pageable);
	}
	
//	public List<Conversation> findAllConvSeq(){
//		return cDao.findAllConvSeq();
//	}
	
	//========== modifying methods =============

	public void save(ChatMessage imMessage){
		Preconditions.checkArgument(imMessage.getId()==null,
				"id should be null");
		
		long seq=incrMaxSeq(imMessage.getReceiver(),imMessage.getSender());
		imMessage.setSeq(seq);
		cmDao.save(imMessage);
		updateReadSeq(imMessage.getSender(),imMessage.getReceiver(),seq);
	}
	
	public void save(long tenantId,ChatMessage imMessage){
		changeTenantSchema(tenantId);
		save(imMessage);
	}
	
	public void deleteBatch(long userId,List<Long> ids){
		cmDao.deleteBatchByReceiver(userId,ids);
		cmDao.deleteBatchBySender(userId,ids);
	}
	
	// ======== conv =========
	
	public Conversation findConversation(long userId,
			long oppositeId){
		return cDao.findByUserIdAndOppositeId(userId,oppositeId);
	}
	
	public Page<Conversation> findConversationsByUserId(long userId,
			Pageable pageable){
		return cDao.findByUserId(userId,pageable);
	}
	
//	@Deprecated
//	private Conversation findConv(long userId,long oppositeId){
//		Conversation conv=cDao.findByUserIdAndOppositeId(userId,oppositeId);
//		if(conv==null){
//			conv=new Conversation();
//			conv.setUserId(userId);
//			conv.setOppositeId(oppositeId);
//			conv.setReadSeq(0L);
//			long maxSeq=Objects.firstNonNull(cmDao.findMaxSeq(userId,oppositeId),0L);
//			conv.setMaxSeq(maxSeq);
//			cDao.save(conv);
//		}
//		return conv;
//	}
	
//	public long getMaxSeq(long userId,long oppositeId){
//		return findConv(userId,oppositeId).getMaxSeq();
//	}
	
	private Conversation initConv(long userId,long oppositeId){
		Conversation conv=new Conversation();
		conv.setUserId(userId);
		conv.setOppositeId(oppositeId);
		conv.setReadSeq(0L);
		conv.setMaxSeq(1L);
		cDao.save(conv);
		return conv;
	}
	
	public long incrMaxSeq(long userId,long oppositeId){
		//TODO make it more atomic
		Conversation conv=findConversation(userId,oppositeId);
		if(conv==null){
			initConv(userId,oppositeId);
			initConv(oppositeId,userId);
			return 1L;
		}
		else{
			cDao.incrMaxSeq(userId,oppositeId);
			return conv.getMaxSeq()+1;
		}
	}
	
//	public long getReadSeq(long userId,long oppositeId){
//		return findConv(userId,oppositeId).getReadSeq();
//	}
	
	public boolean updateReadSeq(long userId,long oppositeId,long seq){
		return cDao.updateReadSeq(userId,oppositeId,seq)>0;
//		Conversation conv=findConversation(userId,oppositeId);
//		if(conv==null){
//			conv=initConv(userId,oppositeId);
//		}
//		if(conv.getReadSeq()<seq && conv.getMaxSeq()>=seq){
//			conv.setReadSeq(seq);
//			cDao.save(conv);
//			return true;
//		}
//		return false;
	}
	
//	public List<Conversation> getAllConv(long userId){
//		String key=RedisKeys.convMaxSeq(userId);
//		Set<String> idSet=redisUtil.hkeys(key);
//		List<Conversation> convList=Lists.newArrayList();
//		for(String str:idSet){
//			Long id=Longs.tryParse(str);
//			if(id!=null){
//				convList.add(new Conversation(userId,id,null));
//			}
//		}
//		return convList;
//	}
	
	public Page<UnreadConversationDigest> getAllUnreadConv(
			final long userId,Pageable pageable){
		
		Page<Conversation> unreadConvs=cDao.findConvsWithUnreadMessages(
				userId,pageable);
		List<UnreadConversationDigest> content=Lists.transform(
				unreadConvs.getContent(),
				new Function<Conversation,UnreadConversationDigest>(){

					@Override
					public UnreadConversationDigest apply(Conversation input){
						ChatMessageDto dto=ChatMessageDto.of(findLastMessage(
								userId,input.getOppositeId()));
						return new UnreadConversationDigest(userId,
								input.getOppositeId(),
								input.getMaxSeq()-input.getReadSeq(),
								dto);
					}
				});
		
		return new PageImpl<UnreadConversationDigest>(content,pageable,
				unreadConvs.getTotalElements());
	}
	
}
