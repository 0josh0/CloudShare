package cn.ac.iscas.oncecloudshare.service.dao.common;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;

public interface SpaceFileFollowDao extends PagingAndSortingRepository<SpaceFileFollow, Long>, JpaSpecificationExecutor<SpaceFileFollow> {
	@Modifying
	@Query("delete SpaceFileFollow t1 where t1.user.id = ?1 and t1.file.id = ?2")
	public void delete(long userId, long fileId);
	
	@Query("FROM SpaceFileFollow t1 where t1.user.id = ?1 and t1.file.id = ?2")
	public SpaceFileFollow findOne(long userId, long fileId);
	
	@Query("FROM SpaceFileFollow t1 where t1.user.id = ?1 and t1.file.id in (?2)")
	public List<SpaceFileFollow> findAll(long userId, List<Long> fileIds);

	@Query("select t1 FROM SpaceFileFollow t1 left join t1.file t2 left join t2.owner t3 where t1.user.id = ?1 and type(t3) = ?2")
	public Page<SpaceFileFollow> findAll(long userId, Class<? extends BaseSpace> spaceType, Pageable pageable);
}