package cn.ac.iscas.oncecloudshare.service.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;

public interface ActionLogDao extends PagingAndSortingRepository<ActionLog, Long>, JpaSpecificationExecutor<ActionLog> {

}
