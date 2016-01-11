package cn.ac.iscas.oncecloudshare.service.extensions.backup.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.backup.model.Backup;

public interface BackupDao extends PagingAndSortingRepository<Backup, Long>, JpaSpecificationExecutor<Backup> {
	@Query("FROM Backup t1 WHERE t1.fileName = ?1")
	Backup findByFileName(String fileName);
}