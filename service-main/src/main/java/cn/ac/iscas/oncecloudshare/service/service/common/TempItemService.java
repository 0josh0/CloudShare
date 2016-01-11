package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.common.TempItemDao;
import cn.ac.iscas.oncecloudshare.service.model.common.TempItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@Service
@Transactional
public class TempItemService {

	private static final int KEY_LENGTH = 32;

	@Autowired
	TempItemDao tiDao;

	public TempItem find(String key) {
		TempItem ti = tiDao.findByKey(key);
		if (ti != null) {
			return checkValid(ti, true);
		}
		return ti;
	}

	/**
	 * 按类型查找
	 * 
	 * @param type
	 * @return
	 */
	public List<TempItem> findByType(String type) {
		Builder<TempItem> builder = ImmutableList.builder();
		for (TempItem item : tiDao.findByType(type)) {
			TempItem tmp = checkValid(item, false);
			if (tmp != null) {
				builder.add(tmp);
			}
		}
		return builder.build();
	}

	/**
	 * 保存一个临时数据
	 * 
	 * @param type
	 *            数据类型
	 * @param content
	 *            数据内容
	 * @param expiresIn
	 *            多长时间过期（毫秒）
	 * @return
	 */
	public TempItem save(String type, String content, long expiresIn) {
		if (expiresIn <= 0) {
			return null;
		}
		TempItem tempItem = new TempItem();
		tempItem.setKey(RandomStringUtils.randomAlphanumeric(KEY_LENGTH));
		tempItem.setType(type);
		tempItem.setContent(content);
		tempItem.setExpiresAt(System.currentTimeMillis() + expiresIn);
		tiDao.save(tempItem);
		return tempItem;
	}

	private TempItem checkValid(TempItem tempItem, boolean deleteIfInvalid) {
		if (tempItem.getExpiresAt() <= System.currentTimeMillis()) {
			if (deleteIfInvalid) {
				tiDao.delete(tempItem);
			}
			return null;
		} else {
			return tempItem;
		}
	}

	public boolean delete(String key) {
		return tiDao.deleteByKey(key) > 0;
	}

	/**
	 * 每3小时清除过期数据
	 */
	@Scheduled(cron = "0 0 */3 * * *")
	public void clearExpiredItem() {
		tiDao.deleteExpiredItem(System.currentTimeMillis());
	}
}
