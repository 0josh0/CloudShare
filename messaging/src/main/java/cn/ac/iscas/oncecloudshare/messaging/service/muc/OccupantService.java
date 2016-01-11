package cn.ac.iscas.oncecloudshare.messaging.service.muc;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;
import cn.ac.iscas.oncecloudshare.messaging.repository.OccupantDao;
import cn.ac.iscas.oncecloudshare.messaging.repository.RoomDao;
import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;

@Service
@Transactional
public class OccupantService extends BaseService{
	
	@Autowired
	RoomDao rDao;
	
	@Autowired
	OccupantDao oDao;

	public MucOccupant find(long roomId,long userId){
		return oDao.findByRoomIdAndUserId(roomId,userId);
	}
	
	public MucOccupant find(long tenantId,long roomId,long userId){
		changeTenantSchema(tenantId);
		return find(roomId,userId);
	}
	
	public Page<MucOccupant> findByRoomId(long roomId,Pageable pageable){
		return oDao.findByRoomId(roomId,pageable);
	}
	
	public Page<MucOccupant> findByRoomId(long tenantId,long roomId,Pageable pageable){
		changeTenantSchema(tenantId);
		return oDao.findByRoomId(roomId,pageable);
	}
	
	public Page<MucOccupant> findByUserId(long userId,Pageable pageable){
		return oDao.findByUserId(userId,pageable);
	}
	
	public Page<MucOccupant> findOccupantsWithUnreadMessages(Long userId,Pageable pageable){
		return oDao.findOccupantsWithUnreadMessages(userId,pageable);
	}
	
	public void updateReadSeq(long roomId,long userId,long readSeq){
		oDao.updateReadSeq(roomId,userId,readSeq);
	}
	
	public void deleteBatch(Long roomId,Set<Long> userIds){
		oDao.deleteBatch(roomId,userIds);
	}
	
	public void delete(MucOccupant occupant){
		oDao.delete(occupant);
	}
}
