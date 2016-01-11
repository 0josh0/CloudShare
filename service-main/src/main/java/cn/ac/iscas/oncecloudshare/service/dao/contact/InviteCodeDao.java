package cn.ac.iscas.oncecloudshare.service.dao.contact;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InviteCodeDao {
	@Modifying
	@Query("DELETE FROM InviteCode WHERE inviter.id = ?1")
	public void delete(long lInviteCodeId);
}
