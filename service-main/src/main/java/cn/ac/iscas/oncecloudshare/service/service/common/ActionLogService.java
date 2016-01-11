package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.action.SystemActionType;
import cn.ac.iscas.oncecloudshare.service.action.SystemTargetType;
import cn.ac.iscas.oncecloudshare.service.dao.common.ActionLogDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;
import cn.ac.iscas.oncecloudshare.service.model.log.ActionType;
import cn.ac.iscas.oncecloudshare.service.model.log.TargetType;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Maps;

@Service
@Transactional(readOnly = true)
public class ActionLogService {
	@Resource
	private ActionLogDao actionLogDao;
	// 用于保存所有的操作对象类型
	private final Map<String, TargetType> targetTypes = Maps.newHashMap();

	@PostConstruct
	public void init() {
		for (TargetType type : SystemTargetType.values()) {
			register(type);
		}
		for (ActionType type : SystemActionType.values()){
			
		}
	}

	public void register(TargetType type) {
		targetTypes.put(type.getCode(), type);
	}

	@Transactional(readOnly = false)
	public ActionLog save(ActionLog actionLog) {
		return actionLogDao.save(actionLog);
	}

	public Page<ActionLog> findAll(List<SearchFilter> filters, Pageable pageable) {
		try {
			return actionLogDao.findAll(Specifications.fromFilters(filters, ActionLog.class), pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
	
	public Collection<TargetType> findAllTargetTypes(){
		return targetTypes.values();
	}
}