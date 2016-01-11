package cn.ac.iscas.oncecloudshare.service.dao.authorization;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.RememberMe;

public interface RememberMeDao extends PagingAndSortingRepository<RememberMe, Long>, JpaSpecificationExecutor<RememberMe> {
	@Query("FROM RememberMe WHERE token = ?1")
	public RememberMe findByToken(String token);
	
	/**
	 * 清除已过期的"记住我"记录
	 *
	 * @return
	 */
	@Modifying
	@Query("DELETE FROM RememberMe WHERE expireAt < CURRENT_TIMESTAMP")
	public int deleteExpired();
}
