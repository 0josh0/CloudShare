package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceFileDao;
import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceFileFollowDao;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.DefaultBusinessException;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

@Service
@Transactional(readOnly = true)
public class SpaceFileFollowService {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(SpaceFileFollowService.class);
	@Resource
	private SpaceFileFollowDao spaceFileFollowDao;
	@Resource
	private SpaceFileDao spaceFileDao;
	@Resource
	private TenantService tenantService;
	
	private LockSet<String> lockSet = new LockSet<>();
	
	private String lockKey(User user, SpaceFile file) {
		return tenantService.getCurrentTenant().getId() + "-" + user.getId() + "-" + file.getId();
	}
	
	@Transactional(readOnly = false)
	public SpaceFileFollow follow(User user, SpaceFile file) {
		Preconditions.checkNotNull(user);
		Preconditions.checkNotNull(file);
		Lock lock = lockSet.getLock(lockKey(user, file));
		lock.lock();
		try {
			SpaceFileFollow follow = findOne(user.getId(), file.getId());
			if (follow != null) {
				throw new DefaultBusinessException(ErrorCode.DUPLICATE_FILE_FOLLOW);
			}
			follow = new SpaceFileFollow(user, file);
			spaceFileFollowDao.save(follow);
			
			spaceFileDao.incrFollows(follow.getFile().getId(), 1);
			
			return follow;
		} finally {
			lock.unlock();
		}
	}
	
	@Transactional(readOnly = false)
	public void unfollow(User user, SpaceFile file) {
		Preconditions.checkNotNull(user);
		Preconditions.checkNotNull(file);
		Lock lock = lockSet.getLock(lockKey(user, file));
		lock.lock();
		try {
			SpaceFileFollow follow = findOne(user.getId(), file.getId());
			if (follow == null) {
				throw new DefaultBusinessException(ErrorCode.FILE_UNFOLLOWED);
			}
			spaceFileFollowDao.delete(follow);
			
			spaceFileDao.incrFollows(follow.getFile().getId(), -1);
		} finally {
			lock.unlock();
		}
	}

	public Page<SpaceFileFollow> findAll(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<SpaceFileFollow> spec = Specifications.fromFilters(filters, SpaceFileFollow.class);
			return spaceFileFollowDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 查询用户对某个文件的收藏记录 
	 *
	 * @param userId
	 * @param fileId
	 * @return
	 */
	public SpaceFileFollow findOne(long userId, long fileId) {
		return spaceFileFollowDao.findOne(userId, fileId);
	}

	/**
	 * 查询用户对某些文件的收藏情况
	 * 
	 * @param userId
	 * @param fileIds
	 * @return
	 */
	public List<SpaceFileFollow> findAll(long userId, List<Long> fileIds) {
		return spaceFileFollowDao.findAll(userId, fileIds);
	}

	/**
	 * 查询用户对某些文件的收藏情况
	 * 
	 * @param userId
	 * @param fileIds
	 * @return
	 */
	public Page<SpaceFileFollow> findAll(long userId, Class<? extends BaseSpace> spaceType, Pageable pageable) {
		return spaceFileFollowDao.findAll(userId, spaceType, pageable);
	}
}
