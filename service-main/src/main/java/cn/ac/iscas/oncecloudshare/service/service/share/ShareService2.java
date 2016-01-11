package cn.ac.iscas.oncecloudshare.service.service.share;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.share.ReceivedShareDao;
import cn.ac.iscas.oncecloudshare.service.dao.share.ShareDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.model.share.ShareRecipient;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
@Transactional(readOnly = true)
public class ShareService2 {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(ShareService2.class);

	@Resource
	private ShareDao shareDao;
	@Resource
	private RuntimeContext runtimeContext;
	@Resource
	private ReceivedShareDao receivedShareDao;
	@Resource
	private DefaultShareRecipientHandler defaultShareRecipientHandler;

	// 用于保存分享接受者处理器
	private Map<String, ShareRecipientHandler> shareRecipientHandlers = Maps.newConcurrentMap();

	@PostConstruct
	public void init() {
		for (String type : defaultShareRecipientHandler.getSupportedRecipientTypes()) {
			shareRecipientHandlers.put(type, defaultShareRecipientHandler);
		}
	}

	/**
	 * 根据过滤条件和分页参数搜索分享
	 * 
	 * @param filters 过滤条件
	 * @param pageParam 分页参数
	 * @return
	 */
	public Page<Share> findAll(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<Share> spec = Specifications.fromFilters(filters, Share.class);
			return shareDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 创建分享
	 * 
	 * @param file
	 * @param targets
	 * @param shareHeadVersion 是否分享最新版本
	 * @param message
	 * @return
	 */
	@Transactional(readOnly = false)
	public Share createShare(User creator, File file, List<ShareRecipient> recipients, boolean shareHeadVersion, String message) {
		// 创建我的分享
		Share share = new Share();
		share.setFile(file);
		share.setCreator(creator);
		share.setDescription(message);
		if (shareHeadVersion) {
			share.setShareHeadVersion();
		} else {
			share.setFileVersion(file.getHeadVersion().getVersion());
		}
		share.setStatus(Share.Status.CREATED);
		share.setRecipients(recipients);
		// 创建接受者收到的分享
		List<ReceivedShare> receivedShares = Lists.newArrayList();
		for (ShareRecipient recipient : recipients) {
			recipient.setShare(share);
			ShareRecipientHandler handler = shareRecipientHandlers.get(recipient.getType());
			if (handler != null) {
				List<ReceivedShare> tmpList = handler.generateReceivedShares(share, recipient);
				joinReceivedShares(receivedShares, tmpList);
			}
		}
		share = shareDao.save(share);
		for (ReceivedShare receivedShare : receivedShares) {
			receivedShareDao.save(receivedShare);
		}
		// TODO: 发送用户分享了某个文件事件

		return share;
	}

	public List<ReceivedShare> joinReceivedShares(List<ReceivedShare> shares1, List<ReceivedShare> shares2) {
		for (ReceivedShare share1 : shares1) {
			for (int j = shares2.size() - 1; j > -1; j--) {
				ReceivedShare share2 = shares2.get(j);
				if (share1.getRecipient().equals(share2.getRecipient())) {
					share1.getBelongsTo().addAll(share2.getBelongsTo());
					shares2.remove(j);
				}
			}
		}
		for (ReceivedShare share2 : shares2) {
			shares1.add(share2);
		}
		return shares1;
	}

	/**
	 * 查找创建者的某个分享
	 * 
	 * @param creatorId 创建者的用户id
	 * @param shareId 创建者的分享id
	 * @return
	 */
	public Share findOne(long creatorId, long shareId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("creator.id", Operator.EQ, creatorId));
		filters.add(new SearchFilter("id", Operator.EQ, shareId));
		filters.add(new SearchFilter("status", Operator.EQ, Share.Status.CREATED));
		List<Share> shares = shareDao.findAll(Specifications.fromFilters(filters, Share.class));
		if (shares != null && shares.size() > 0) {
			return shares.get(0);
		}
		return null;
	}

	/**
	 * 撤销分享
	 * 
	 * @param share
	 */
	@Transactional(readOnly = false)
	public void cancelShare(Share share) {
		share.setStatus(Share.Status.CANCELED);
		share.setCancelTime(new Date());
		shareDao.save(share);
	}

	public Page<Share> findAll(long creatorId, String recipientType, Pageable pageable) {
		return shareDao.findAll(creatorId, recipientType, pageable);
	}
	
	/**
	 * 通过接受者的类别和id查询用户分享
	 *
	 * @param masterId 用户id
	 * @param recipientType 接受者类别
	 * @param recipientId 接受者id
	 * @param pageable 分页参数
	 * @return
	 */
	public Page<Share> findAll(Long masterId, String recipientType, Long recipientId, Pageable pageable) {
		return shareDao.findAll(masterId, recipientType, recipientId, pageable);
	}
}
