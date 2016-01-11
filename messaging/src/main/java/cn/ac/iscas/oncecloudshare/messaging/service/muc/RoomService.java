package cn.ac.iscas.oncecloudshare.messaging.service.muc;

import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Occupant;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucRoom;
import cn.ac.iscas.oncecloudshare.messaging.repository.OccupantDao;
import cn.ac.iscas.oncecloudshare.messaging.repository.RoomDao;
import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;
import cn.ac.iscas.oncecloudshare.messaging.utils.concurrent.LockSet;

import com.google.common.base.Preconditions;

@Service
@Transactional
public class RoomService extends BaseService{
	
	@Autowired
	RoomDao rDao;

	@Autowired
	OccupantDao oDao;
	
	LockSet<Long> roomLockSet=new LockSet<Long>();
	
	public MucRoom findOne(Long id){
		return rDao.findOne(id);
	}
	
	public MucRoom findOne(long tenantId,Long id){
		changeTenantSchema(tenantId);
		return rDao.findOne(id);
	}
	
	public Page<MucRoom> findAll(Pageable pageable){
		return rDao.findAll(pageable);
	}
	
	public Page<MucRoom> findByOwner(Long ownerId,Pageable pageable){
		return rDao.findByOwner(ownerId,pageable);
	}
	
	public Page<MucRoom> findByUser(Long userId,Pageable pageable){
		return rDao.findByUser(userId,pageable);
	}
	
//	public Page<MucRoom> findRoomsWithUnreadMessages(Long userId,Pageable pageable){
//		return rDao.findRoomsWithUnreadMessages(userId,pageable);
//	}
	
	public MucRoom add(MucRoom room,Set<MucOccupant> occupants){
		Preconditions.checkArgument(room.getId()==null);
		rDao.save(room);
		addOccupants(room,occupants);
		return room;
	}
	
	public void addOccupants(MucRoom room,Set<MucOccupant> occupants){
		Preconditions.checkArgument(room.getId()!=null);
		for(MucOccupant occupant:occupants){
			occupant.setRoom(room);
			occupant.setId(null);
			occupant.setReadSeq(room.getMaxSeq());
		}
		oDao.save(occupants);
	}
	
	public void upadte(MucRoom room){
		Preconditions.checkArgument(room.getId()!=null);
		rDao.save(room);
	}
	
	public boolean changeOwner(MucRoom room,long ownerId){
		if(room.getOwner().equals(ownerId)){
			return false;
		}
		MucOccupant o=oDao.findByRoomIdAndUserId(room.getId(),ownerId);
		if(o==null){
			return false;
		}
		o.setRole(Role.Moderator);
		oDao.save(o);
		
		MucOccupant o2=oDao.findByRoomIdAndUserId(room.getId(),room.getOwner());
		if(o2!=null){
			o2.setRole(Role.Participant);
			oDao.save(o2);
		}
		
		room.setOwner(ownerId);
		rDao.save(room);
		return true;
	}
	
	public Long incrMaxSeq(MucRoom room){
		Lock lock=roomLockSet.getLock(room.getId());
		try{
			lock.lock();
			room.setMaxSeq(room.getMaxSeq()+1);
			upadte(room);
			return room.getMaxSeq();
		}
		finally{
			lock.unlock();
		}
	}
	
	public void delete(Long roomId){
		rDao.delete(roomId);
	}
	
	public void safeDelete(Long roomId,Long userId){
		rDao.safeDelete(roomId,userId);
	}
}
