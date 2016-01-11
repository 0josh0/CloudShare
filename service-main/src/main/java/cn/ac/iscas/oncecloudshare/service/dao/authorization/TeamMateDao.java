package cn.ac.iscas.oncecloudshare.service.dao.authorization;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;

public interface TeamMateDao  extends PagingAndSortingRepository<TeamMate, Long>, JpaSpecificationExecutor<TeamMate>{
}