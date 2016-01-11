package cn.ac.iscas.oncecloudshare.service.dao.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam.Status;

public interface BaseTeamDao extends PagingAndSortingRepository<BaseTeam, Long>, JpaSpecificationExecutor<BaseTeam> {
	@Query("select t1 from BaseTeam t1 inner join t1.members t2 where t2.user.id = ?1 and t1.status = ?2")
	Page<Team> findJoined(long userId, Status status, Pageable pageable);

	@Modifying
	@Query("UPDATE BaseTeam t1 set t1.membersCount = t1.membersCount + (?2) WHERE t1.id = ?1")
	void updateMemebersCount(long teamId, int increment);
}