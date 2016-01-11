package cn.ac.iscas.oncecloudshare.monitor.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.monitor.model.Server;

public interface ServerDao extends PagingAndSortingRepository<Server, Long>, JpaSpecificationExecutor<Server> {

}
