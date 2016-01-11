package cn.ac.iscas.oncecloudshare.service.extensions.company.space.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpace;

public interface CompanySpaceDao extends PagingAndSortingRepository<CompanySpace, Long>, JpaSpecificationExecutor<CompanySpace> {

}
