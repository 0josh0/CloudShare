package cn.ac.iscas.oncecloudshare.service.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;

@Repository
public interface SpaceFileVersionDao extends PagingAndSortingRepository<SpaceFileVersion, Long>, JpaSpecificationExecutor<SpaceFileVersion> {
}