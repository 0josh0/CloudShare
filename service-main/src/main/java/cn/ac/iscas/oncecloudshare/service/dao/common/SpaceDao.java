package cn.ac.iscas.oncecloudshare.service.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;

public interface SpaceDao  extends PagingAndSortingRepository<BaseSpace, Long>, JpaSpecificationExecutor<BaseSpace>{
	/**
	 * 增加restQuota
	 * @param id 空间id
	 * @param increment 增加量（可以为负数）
	 */
	@Modifying
	@Query("UPDATE BaseSpace u SET u.restQuota=u.restQuota+(?2) WHERE u.id=?1 AND u.restQuota+(?2)>=0")
	int incrRestQuota(Long id, long increment);
}