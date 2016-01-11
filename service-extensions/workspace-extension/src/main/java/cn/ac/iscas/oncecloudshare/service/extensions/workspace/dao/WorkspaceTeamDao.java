package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceTeam;

public interface WorkspaceTeamDao extends PagingAndSortingRepository<WorkspaceTeam, Long>, JpaSpecificationExecutor<WorkspaceTeam> {
}