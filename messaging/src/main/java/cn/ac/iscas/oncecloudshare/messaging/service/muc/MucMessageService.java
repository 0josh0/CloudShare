package cn.ac.iscas.oncecloudshare.messaging.service.muc;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.messaging.dto.muc.MucMessageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.muc.UnreadMucMessageDigest;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucMessage;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucRoom;
import cn.ac.iscas.oncecloudshare.messaging.repository.MucMessageDao;
import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


@Service
@Transactional
public class MucMessageService extends BaseService{

	@Autowired
	MucMessageDao mmDao;
	
	@Autowired
	RoomService rService;
	
	@Autowired
	OccupantService oService;
	
	public Page<MucMessage> findByRoomIdAndTsGreaterThan(long roomId,
			long ts,Pageable pageable){
		return mmDao.findByRoomIdAndTsGreaterThan(roomId,ts,pageable);
	}
	
	public Page<MucMessage> searchMessagesInRoom(String keyword,long roomId,
			long begin,long end,Pageable pageable){
		return mmDao.searchMessagesInRoom(keyword,roomId,begin,end,pageable);
	}
	
	public MucMessage findLastMessage(long roomId){
		Iterator<MucMessage> it=findByRoomIdAndTsGreaterThan(roomId,0,
				new PageRequest(0,1,Direction.DESC,"ts")).iterator();
		return it.hasNext()?it.next():null;
	}
	
	public Page<UnreadMucMessageDigest> findUnreadMucMessages(long userId,
			Pageable pageable){
		Page<MucOccupant> page=oService.findOccupantsWithUnreadMessages(userId,pageable);
		List<UnreadMucMessageDigest> content=Lists.transform(page.getContent(),
				new Function<MucOccupant,UnreadMucMessageDigest>(){

					@Override
					public UnreadMucMessageDigest apply(MucOccupant input){
						MucRoom room=input.getRoom();
						//TODO find all last messages in one query
						MucMessage message=findLastMessage(room.getId());
						return new UnreadMucMessageDigest(room.getId(),
								room.getMaxSeq()-input.getReadSeq(),
								MucMessageDto.of(message));
					}
				});
		return new PageImpl<UnreadMucMessageDigest>(content,
				pageable,page.getTotalElements());
	}
	
	public void save(MucMessage message){
		Preconditions.checkArgument(message.getId()==null,
				"id should be null");
		MucRoom room=rService.findOne(message.getRoomId());
		message.setSeq(rService.incrMaxSeq(room));
		mmDao.save(message);
		oService.updateReadSeq(message.getRoomId(),message.getSender(),message.getSeq());
	}
	
	public void save(long tenantId,MucMessage message){
		changeTenantSchema(tenantId);
		save(message);
	}
	
	public void deleteByRoomId(Long roomId){
		mmDao.deleteByRoomId(roomId);
	}
}
