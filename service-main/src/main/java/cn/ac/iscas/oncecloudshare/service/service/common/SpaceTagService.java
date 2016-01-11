package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceFileDao;
import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceTagDao;
import cn.ac.iscas.oncecloudshare.service.dto.file.UpdateTagOrderReq;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceTag;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true, rollbackFor = Throwable.class)
public class SpaceTagService {
	@Resource(name = "spaceTagDao")
	private SpaceTagDao tagDao;
	@Resource(name = "spaceFileDao")
	private SpaceFileDao fileDao;
	
	private LockSet<Long> lockSet = new LockSet<Long>();

	public SpaceTag findOne(long id) {
		return tagDao.findOne(id);
	}

	public SpaceTag findOne(long ownerId, String title) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, ownerId));
		filters.add(new SearchFilter("title", Operator.EQ, title));
		return tagDao.findOne(Specifications.fromFilters(filters, SpaceTag.class));
	}
	
	public List<SpaceTag> findAll(List<SearchFilter> filters) {
		return tagDao.findAll(Specifications.fromFilters(filters, SpaceTag.class));
	}

	public Page<SpaceTag> findAll(List<SearchFilter> filters, Pageable pageable) {
		return tagDao.findAll(Specifications.fromFilters(filters, SpaceTag.class), pageable);
	}

	@Transactional(readOnly = false)
	public SpaceTag save(SpaceTag tag) {
		return tagDao.save(tag);
	}

	@Transactional(readOnly = false)
	public void updateOrders(UpdateTagOrderReq req) {
		for (int i = 0, ii = req.getTags().length; i < ii; i++) {
			Lock lock = lockSet.getLock(req.getTags()[i]);
			lock.lock();
			try {
				tagDao.updateOrder(req.getTags()[i], req.getOrders()[i]);
			} finally {
				lock.unlock();
			}
		}
	}

	@Transactional(readOnly = false)
	public void delete(SpaceTag tag) {
		fileDao.deleteTag(tag.getId());
		Lock lock = lockSet.getLock(tag.getId());
		lock.lock();
		try {
			tagDao.delete(tag);
		} finally {
			lock.unlock();
		}
	}
	
	@Transactional(readOnly = false)
	public void updateFilesCount(long tagId, long increment){
		Lock lock = lockSet.getLock(tagId);
		lock.lock();
		try{
			tagDao.updateFilesCount(tagId, increment);
		} finally{
			lock.unlock();
		}
	}
}
