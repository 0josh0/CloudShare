package cn.ac.iscas.oncecloudshare.service.service.filemeta;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.filemeta.FileDao;
import cn.ac.iscas.oncecloudshare.service.dao.filemeta.TagDao;
import cn.ac.iscas.oncecloudshare.service.dto.file.UpdateTagOrderReq;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.Tag;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true, rollbackFor = Throwable.class)
public class TagService {
	@Resource
	private TagDao tagDao;
	@Resource
	private FileDao fileDao;

	public Tag findOne(long id) {
		return tagDao.findOne(id);
	}

	public Tag findOne(long ownerId, String title) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, ownerId));
		filters.add(new SearchFilter("title", Operator.EQ, title));
		return tagDao.findOne(Specifications.fromFilters(filters, Tag.class));
	}

	public Page<Tag> findAll(List<SearchFilter> filters, Pageable pageable) {
		return tagDao.findAll(Specifications.fromFilters(filters, Tag.class), pageable);
	}

	@Transactional(readOnly = false)
	public Tag save(Tag tag) {
		return tagDao.save(tag);
	}

	@Transactional(readOnly = false)
	public void updateOrders(UpdateTagOrderReq req) {
		for (int i = 0, ii = req.getTags().length; i < ii; i++) {
			tagDao.updateOrder(req.getTags()[i], req.getOrders()[i]);
		}
	}

	@Transactional(readOnly = false)
	public void delete(Tag tag) {
		fileDao.deleteTag(tag.getId());
		tagDao.delete(tag);
	}
	
	@Transactional(readOnly = false)
	public void updateFilesCount(long tagId, long increment){
		tagDao.updateFilesCount(tagId, increment);
	}
}
