package cn.ac.iscas.oncecloudshare.service.dao.authorization;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;



public interface UserDao extends PagingAndSortingRepository<User, Long>, JpaSpecificationExecutor<User> {
	
	
	@Modifying
	@Query("UPDATE User u SET u.signature =?1 WHERE u.id =?2")
	public int setSignature(String signature, long id);
	
	@Query("FROM User WHERE email=?1 AND status!='DELETED'")
	public User findByEmail(String email);

//	@Query("select u.id from User as u")
//	List<Long> getAllUserIds();

//	@Query("select u from User as u where u.status=?1")
//	List<User> findUserByStatus(UserStatus us);
	
	/**
	 * 增加restQuota
	 * @param userId 用户id
	 * @param increment 增加量（可以为负数）
	 */
	@Modifying
	@Query("UPDATE User u SET u.restQuota=u.restQuota+(?2) WHERE u.id=?1 AND u.restQuota+(?2)>=0")
	int incrRestQuota(long userId,long increment);
	
	/**
	 * 增加quota和restQuota
	 * @param userId 用户id
	 * @param increment 增加量（可以为负数）
	 */
	@Modifying
	@Query("UPDATE User u SET u.quota=u.quota+(?2),u.restQuota=u.restQuota+(?2) WHERE u.id=?1 AND u.restQuota+(?2)>=0")
	int incrQuota(long userId,long increment);
	
	@Modifying
	@Query("UPDATE User u set u.department=null WHERE u.department.id IN (?1)")
	void detachDepartment(List<Long> deptIdList);

	@Modifying
	@Query("update User u set u.status = ?2 where u.id = ?1")
	void updateStatus(Long id,UserStatus status);

//	@Modifying
//	@Query("update User u set u.quota = ?1 where u.id = ?2")
//	void setFixedQuotaFor(Long quota, Long id);

//	@Query("select u from User as u join u.department as ud where ud.route like ?1 and u.status != ?2")
//	List<User> selectUserDeptNameStartingWith(String deptRoute, UserStatus userStatus);
//
//	@Query("select u from User as u join u.department as ud where ud.route like ?1 and u.status = ?2")
//	List<User> findByDeptAndStatus(String deptRoute, UserStatus userStatus);
//
//	@Query("select u from User as u join u.department as ud where u.email like ?1 and ud.route like ?2 and u.status != 'DISACTIVE'")
//	List<User> filterUserByDeptAndEmail(String email,String deptRoute);
//
//	@Query("select u from User as u join u.department as ud where u.name like ?1 and ud.route like ?2 and u.status != 'DISACTIVE'")
//	List<User> filterUserByDeptAndName(String name,String deptRoute);

}
