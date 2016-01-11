package cn.ac.iscas.oncecloudshare.service.dao.authorization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.UserProfile;


public interface UserProfileDao extends PagingAndSortingRepository<UserProfile, Long>{

	@Query("select up from UserProfile as up join up.user as u where u.id=?1")
	UserProfile findByUserId(long userId);
}
