package cn.ac.iscas.oncecloudshare.service.service.share;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.share.ReceivedShareDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class ReceivedShareService {
	@Resource
	private ReceivedShareDao receivedShareDao;

	public ReceivedShare findOne(long id) {
		return receivedShareDao.findOne(id);
	}

	public Page<ReceivedShare> findAll(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<ReceivedShare> spec = Specifications.fromFilters(filters, ReceivedShare.class);
			return receivedShareDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	@Transactional(readOnly = false)
	public void delete(ReceivedShare share) {
		if (!share.getIsDeleted()) {
			share.setIsDeleted(true);
			receivedShareDao.save(share);
		}
	}

	/**
	 * 查询某个用户收到的分享
	 * 
	 * @param masterId 用户id
	 * @param recipientType 接受者类别
	 * @param recipientId 接受者id。比如分享给department的，这个值就是departmentId
	 * @param pageable 分页参数
	 * @return
	 */
	public Page<ReceivedShare> findAll(Long masterId, String recipientType, Long recipientId, Pageable pageable) {
		return receivedShareDao.findAll(masterId, recipientType, recipientId, pageable);
	}

	/**
	 * 查询某个用户收到的分享
	 * 
	 * @param masterId 用户id
	 * @param recipientType 接受者类别
	 * @param pageable 分页参数
	 * @return
	 */
	public Page<ReceivedShare> findAll(Long masterId, String recipientType, Pageable pageable) {
		return receivedShareDao.findAll(masterId, recipientType, pageable);
	}

	/**
	 * 查看某个分享对应的所有收到的分享
	 * 
	 * @param shareId
	 * @return
	 */
	public List<ReceivedShare> findAll(Share share) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("share", Operator.EQ, share));
		return receivedShareDao.findAll(Specifications.fromFilters(filters, ReceivedShare.class));
	}
}
