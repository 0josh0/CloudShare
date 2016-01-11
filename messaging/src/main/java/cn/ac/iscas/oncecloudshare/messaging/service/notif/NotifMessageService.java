package cn.ac.iscas.oncecloudshare.messaging.service.notif;

import java.util.List;

import net.sf.ehcache.search.SearchException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;
import cn.ac.iscas.oncecloudshare.messaging.repository.NotifMessageDao;
import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.Specifications;

@Service
public class NotifMessageService extends BaseService{

	@Autowired
	NotifMessageDao nmDao;
	
	public NotifMessage findOne(Long id){
		NotifMessage notifMessage=nmDao.findOne(id);
		if(notifMessage.getDel()){
			return null;
		}
		return notifMessage;
	}
	
	public Page<NotifMessage> findByReceiver(Long receiver,Pageable pageable){
		return nmDao.findByReceiver(receiver,pageable);
	}
	
	public Page<NotifMessage> search(List<SearchFilter> filters,Pageable pageable){
		try{
			filters.add(new SearchFilter("del",Operator.EQ,false));
			Specification<NotifMessage> spec=Specifications
					.fromFilters(filters,NotifMessage.class);
			return nmDao.findAll(spec,pageable);
		}
		catch(Exception e){
			throw new SearchException(e);
		}
	}
	
//	public Long countUnread(Long receiver){
//		return nmDao.countUnread(receiver);
//	}
	
	//========== modifying methods =============

	@Transactional
	public void save(NotifMessage notifMessage){
		nmDao.save(notifMessage);
	}
	
	@Transactional
	public void save(long tenantId,NotifMessage notifMessage){
		changeTenantSchema(tenantId);
		nmDao.save(notifMessage);
	}
	
	@Transactional
	public void markAsReadBatch(Long receiver,List<Long> ids){
		nmDao.markAsReadBatch(receiver,ids);
	}
	
	@Transactional
	public void deleteBatch(Long receiver,List<Long> ids){
		nmDao.deleteBatch(receiver,ids);
	}
}
