package cn.ac.iscas.oncecloudshare.service.dao.multitenancy;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;


public interface TennatDao extends PagingAndSortingRepository<Tenant,Long>, JpaSpecificationExecutor<Tenant>{

}
