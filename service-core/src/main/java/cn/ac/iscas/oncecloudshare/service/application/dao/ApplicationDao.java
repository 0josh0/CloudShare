package cn.ac.iscas.oncecloudshare.service.application.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.application.model.Application;

public interface ApplicationDao<T extends Application> extends PagingAndSortingRepository<T, Long>, JpaSpecificationExecutor<T> {

}