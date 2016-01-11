package cn.ac.iscas.oncecloudshare.service.extensions.company.space.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpaceApplication;

public interface CompanySpaceApplicationDao extends PagingAndSortingRepository<CompanySpaceApplication, Long>,
		JpaSpecificationExecutor<CompanySpaceApplication> {

}