package cn.ac.iscas.oncecloudshare.service.dao.authorization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam.Status;

public interface TeamDao extends PagingAndSortingRepository<Team, Long>, JpaSpecificationExecutor<Team> {
	@Query("select t1 from Team t1 inner join t1.members t2 where t2.user.id = ?1 and t1.status = ?2")
	Page<Team> findJoined(long userId, Status status, Pageable pageable);

	@Modifying
	@Query("UPDATE Team t1 set t1.membersCount = t1.membersCount + (?2) WHERE t1.id = ?1")
	void updateMemebersCount(long teamId, int increment);
}