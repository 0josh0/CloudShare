package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;

public interface WorkspaceDao extends PagingAndSortingRepository<Workspace, Long>, JpaSpecificationExecutor<Workspace> {
	@Query("from Workspace t1 where t1.team.id in (?1) and t1.status = 'active'")
	public Page<Workspace> listByTeamIds(List<Long> teamIds, Pageable pageable);

	@Query("select t1 from Workspace t1 inner join t1.team.members t2 where t2.user.id = ?1 and t1.status = ?2")
	public Page<Workspace> findJoined(long userId, String status, Pageable pageable);
}