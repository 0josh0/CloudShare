package cn.ac.iscas.oncecloudshare.exts.service;

import java.io.File;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.exts.dao.ExtensionDao;
import cn.ac.iscas.oncecloudshare.exts.model.Extension;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class ExtensionServcie {
	@Resource
	private ExtensionDao extensionDao;
	@Resource
	private ConfigService configService;

	public Page<Extension> findAll(List<SearchFilter> filters, Pageable pageable) {
		try {
			return extensionDao.findAll(Specifications.fromFilters(filters, Extension.class), pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public Extension findOne(String name, String version) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("name", Operator.EQ, name));
		filters.add(new SearchFilter("version", Operator.EQ, version));
		return extensionDao.findOne(Specifications.fromFilters(filters, Extension.class));
	}

	public Extension findOne(long id) {
		return extensionDao.findOne(id);
	}

	@Transactional(readOnly = false)
	public Extension save(Extension extension) {
		return extensionDao.save(extension);
	}

	public File getSaveDir() {
		String filePath = configService.getConfig(Configs.Keys.EXT_DIR, FileUtils.getTempDirectoryPath());
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}