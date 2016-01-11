package cn.ac.iscas.oncecloudshare.service.dao.filemeta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileComment;

public interface FileCommentDao extends PagingAndSortingRepository<FileComment,Long>, JpaSpecificationExecutor<FileComment> {

	@Query("FROM FileComment f WHERE f.file.id=?1 ORDER BY f.createTime DESC ")
	Page<FileComment> commentList(long file,Pageable pageable);
}
