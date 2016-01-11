package cn.ac.iscas.oncecloudshare.exts.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.exts.model.Extension;

public interface ExtensionDao extends PagingAndSortingRepository<Extension, Long>, JpaSpecificationExecutor<Extension> {

}