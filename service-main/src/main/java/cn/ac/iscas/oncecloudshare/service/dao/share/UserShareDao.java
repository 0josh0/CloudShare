package cn.ac.iscas.oncecloudshare.service.dao.share;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.share.UserShare;

public interface UserShareDao extends PagingAndSortingRepository<UserShare, Long>, JpaSpecificationExecutor<UserShare>  {

	public List<UserShare> findByFileId(long fileId);

	public UserShare findByFileIdAndOwnerIdAndRecipientId(long fileId, long ownerId, long recipientId);
}
