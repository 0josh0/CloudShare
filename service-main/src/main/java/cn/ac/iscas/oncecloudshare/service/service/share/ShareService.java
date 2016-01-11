package cn.ac.iscas.oncecloudshare.service.service.share;

import java.text.ParseException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.share.LinkShareDao;
import cn.ac.iscas.oncecloudshare.service.dao.share.UserShareDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.share.LinkShare;
import cn.ac.iscas.oncecloudshare.service.model.share.UserShare;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

//Spring Bean的标识.
@Component
@Transactional(readOnly = false)
public class ShareService {
	static Logger _logger = LoggerFactory.getLogger(ShareService.class);
	
	@Resource
	private LinkShareDao linkShareDao;
	@Resource
	private UserShareDao userShareDao;
	/*private static final String ROOT_DIR = "/";
	private UserDao userDao;
	private FileDao fileDao;*/
	/*private UserShareDao userShareDao;
	private DeptShareDao deptShareDao;
	@Autowired
	private TeamShareDao teamShareDao;
	private UserFileAssociationDao userFileAssociationDao;
	private DepartmentDao departmentDao;
	
	@Autowired
	private TeamDao teamDao;
	@Autowired
	private TeamMateDao teamMateDao;

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Autowired
	public void setFileDao(FileDao fileDao) {
		this.fileDao = fileDao;
	}*/

	/*@Autowired
	public void setUserShareDao(UserShareDao userShareDao) {
		this.userShareDao = userShareDao;
	}

	@Autowired
	public void setDeptShareDao(DeptShareDao deptShareDao) {
		this.deptShareDao = deptShareDao;
	}

	@Autowired
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	@Autowired
	public void setUserFileAssociationDao(
			UserFileAssociationDao userFileAssociationDao) {
		this.userFileAssociationDao = userFileAssociationDao;
	}

	*//******************************
	 * 
	 * share list api
	 * 
	 * *****************************//*
	public Page<File> getFileList(Pageable pageRequest, long fileId) {
		File f = fileDao.findOne(fileId);
		Page<File> page = fileDao.findUserFileList(fileId, Relation.OWN,
				FileStatus.HEALTHY, f.reachOwner().getId(), pageRequest);
		return page;
	}

	public boolean canUserAccessOwnerFile(File f, long ownerId, long userId) {

		// here are two share cases.
		// first DeptShare case || second UserShare case.
		if (this.ifCanAccessInDeptShare(f, ownerId, userId)
				|| this.ifCanAccessInUserShare(f, ownerId, userId))
			return true;
		return false;
	}

	*//******************************
	 * 
	 * user share
	 * 
	 * *****************************//*
	public void saveUserShare(long fileId, long userId) {
		int canceled = 1;
		int uncancel = 0;
		UserShare us = new UserShare();

		us.setFile(fileDao.findOne(fileId));
		us.setUser(userDao.findOne(userId));
		us.setCreateTime(Calendar.getInstance().getTime());
		us.setOtherCancel(uncancel);
		us.setOwnerCancel(uncancel);
		us.setOwner((us.getFile().reachOwner()));
		us.setModify(0);

		userShareDao.save(us);
		System.out.println("save end!");
	}

	public UserShare cancelUserShare(long shareId, long userId) {
		int ownerCancel = 0;
		int otherCancel = 1;
		int canceled = 1;
		UserShare us = new UserShare();
		us = userShareDao.findOne(shareId);
		// if (us.getOwner().getId() == userId) {
		// us.setOwnerCancel(canceled);
		// } else if (us.getUser().getId() == userId) {
		// us.setOtherCancel(canceled);
		// }

		// if ((us.getOtherCancel() & us.getOwnerCancel()) == canceled) {
		userShareDao.delete(us);
		// } else
		// userShareDao.save(us);
		return us;
	}

	public Page<UserShare> listUserShare(long userId, Pageable pageRequest,
			int selfSend) {
		if (selfSend == 1)
			return userShareDao.findByUserAsOwner(userId, FileStatus.HEALTHY,pageRequest);
		else if (selfSend == 0)
			return userShareDao.findByUser(userId, FileStatus.HEALTHY,pageRequest);
		return null;
	}

	public UserShare findUserShareBy(long fileId, long userId, long ownerId) {
		return userShareDao.findByUserAndOwnerAndFileId(fileId, ownerId, userId);
	}

	public int cancelUserShareByFileId(long fileId) {
		List<UserShare> list = userShareDao.findByFileId(fileId);
		// userShareDao.deleteByFileId(fileId);
		for (UserShare us : list) {
			userShareDao.delete(us.getId());
		}
		return list.size();
	}

	*//******************************
	 * 
	 * department share
	 * 
	 * *****************************//*
	public void saveDeptShare(long fileId, long deptId) {
		DeptShare deptShare = new DeptShare();

		deptShare.setCreateTime(Calendar.getInstance().getTime());
		deptShare.setDepartment(departmentDao.findOne(deptId));
		deptShare.setFile(fileDao.findOne(fileId));

		deptShareDao.save(deptShare);

		//System.out.println("deptShare save end!");
	}

	public DeptShare findDeptShare(long fileId, long deptId) {
		return deptShareDao.findByDeptAndFileId(fileId, deptId);
	}

	public DeptShare cancelDeptShare(long shareId) {
		DeptShare ds = deptShareDao.findOne(shareId);
		deptShareDao.delete(ds.getId());
		System.out.println("deptShare delete end!" + deptShareDao.count());
		return ds;
	}

	public int cancelDeptShareByFileId(long fileId) {
		List<DeptShare> list = deptShareDao.findByFileId(fileId);
		// deptShareDao.deleteShareByFileId(fileId);
		for (DeptShare ds : list)
			deptShareDao.delete(ds.getId());
		//System.out.println("all deptShare relate to fileId=" + fileId+ ". Delete end!" + deptShareDao.count());
		return list.size();
	}

	public Page<DeptShare> listDeptShare(long deptId, long userId,
			Pageable pageRequest, int selfShare) {
		// params in this function is correct
		Page<DeptShare> deptSharelist = null;
		if (selfShare == 0)
			deptSharelist = deptShareDao.findByDept(deptId, pageRequest);
		else if (selfShare == 1)
			deptSharelist = deptShareDao.findUserDeptShare(deptId, userId,
					pageRequest);
		return deptSharelist;
	}

	public int cancelTeamShareByFileId(long fileId) {
		List<TeamShare> list = teamShareDao.findTeamShareByFileId(fileId);
		// deptShareDao.deleteShareByFileId(fileId);
		for (TeamShare ts : list)
			teamShareDao.delete(ts.getId());
		return list.size();
	}*/

	/******************************
	 * 
	 * external share
	 * 
	 * @throws ParseException
	 * 
	 * *****************************/
	public LinkShare saveLinkShare(LinkShare es){
		return linkShareDao.save(es);
	}

	/*public String checkAvailabilityOfExternalFile(long externalId, String pickUp) {
		LinkShare es = sharedLinkDao.findByKey(externalId);
		String result = "";
		if (es == null) {
			result = "请求的共享文件不存在";
		} else if (!es.getPickUp().equals(pickUp)) {
			result="提取码错误";
		} else if (es.getExpireTime().compareTo(DateTimeUtil.getNow()) < 0) {
			result="共享链接已过期";
		}
		return result;
	}

	public LinkShare getExternalShareByExternalIdAndPickUp(long externalId,
			String pickUp) {
		LinkShare es = sharedLinkDao.findByKey(externalId);
		if (es == null) {
			throw new ShareNotExistException("请求的共享文件不存在");
		}
		if (!es.getPickUp().equals(pickUp)) {
			throw new SharePickUpErrorException("提取码错误");
		}
		if (es.getExpireTime().compareTo(DateTimeUtil.getNow()) < 0) {
			throw new ShareExpiredException("共享链接已过期");
		}
		return es;
	}*/
	
	public void updateLinkShareDownloads(String key){
		linkShareDao.updateDownloads(key);
	}

	public LinkShare getLinkShareByKey(String key) {
		return linkShareDao.findByKey(key);
	}

	/*public LinkShare getExternalShareByShareIdAndUserId(long shareId,
			long userId) {
		LinkShare es = sharedLinkDao.findByShareId(shareId);
		if (es == null) {
			throw new ShareNotExistException("请求的共享文件不存在");
		}
		if (es.getUser().getId() != userId) {
			throw new FileNotAuthorizedException("文件未授权");
		}
		return es;
	}

	*/
	
	public LinkShare getLinkShareById(long shareId) {
		return linkShareDao.findOne(shareId);
	}

	public Page<LinkShare> getExternalShareListByUserId(long userId,
			int page, int size, String direction, String orderBy) {
		Sort sort = new Sort(Direction.fromString(direction), orderBy);
		Pageable pageRequest = new PageRequest(page, size, sort);
		return linkShareDao.findByUserId(userId, pageRequest);
	}

	public Page<File> getLinkShareFilesByUserId(long userId, Pageable pageable) {
		return linkShareDao.findShareFilesByUserId(userId, pageable);
	}

	public Page<LinkShare> getExternalSharesByUserIdAndFileId(long userId,
			long fileId, int page, int size, String direction, String orderBy) {
		Sort sort = new Sort(Direction.fromString(direction), orderBy);
		Pageable pageRequest = new PageRequest(page, size, sort);
		return linkShareDao.findSharesByUserIdAndFileId(userId, fileId,
				pageRequest);
	}
	
	public void deleteLinkShare(LinkShare sharedLink) {
		linkShareDao.delete(sharedLink);
	}

	/******************************
	 * 
	 * local functions
	 * 
	 * *****************************/
	/*public boolean ifCanAccessInUserShare(File f, long ownerId, long userId) {
		// TODO:in the future, the admin case will be added
		if (ownerId == userId) {
			// this is the owner visit file. no problem
			return true;
		}
		while (f != null) {
			if (userShareDao.findByUserAndOwnerAndFileId(f.getId(), ownerId,
					userId) != null) {
				// the file contain in one userShare between user and owner
				System.out.println("access by user");
				return true;
			}
			f = f.getParent();
		}

		// userShare not find and user can not access the file.
		return false;
	}

	public boolean ifCanAccessInDeptShare(File f, long ownerId, long userId) {
		// TODO:in the future, the admin case will be added

		if (ownerId == userId) {
			// this is the owner visit file. no problem
			return true;
		}
		Department userDept = this.userDao.findOne(userId).getDepartment();
		// find all deptShares relate to file or its parent
		List<DeptShare> fileDeptList = this.deptShareDao
				.findByFileId(f.getId());
		while (f.getParent() != null) {
			f = f.getParent();
			List<DeptShare> curFileDeptShareList = this.deptShareDao
					.findByFileId(f.getId());
			if (curFileDeptShareList != null)
				fileDeptList.addAll(curFileDeptShareList);
		}

		// find dept of the user and its parents
		List<Long> userDeptIdRoute = new ArrayList<Long>();
		while (userDept != null) {
			userDeptIdRoute.add(userDept.getId());
			userDept = userDept.getParent();
		}

		for (DeptShare ds : fileDeptList) {
			if (userDeptIdRoute.contains(ds.getDepartment().getId())) {
				// System.out.println("access by dept");
				return true;
			}
		}
		return false;
	}

	public UserShare findUserShare(long fileId, long ownerId, long userId) {
		return userShareDao
				.findByUserAndOwnerAndFileId(fileId, ownerId, userId);
	}

	public UserShare findUserShare(long fileId, long userId) {
		return userShareDao.findByUserAndFileId(fileId, userId);
	}

	public List<DeptShare> getDeptShareByFile(long fileId) {
		return deptShareDao.findByFileId(fileId);
	}

	public List<UserShare> getUserShareByFile(long fileId) {
		return userShareDao.findByFileId(fileId);
	}

	public UserShare findUserShareById(long shareId) {
		// TODO Auto-generated method stub
		return userShareDao.findOne(shareId);
	}

	public DeptShare findDeptShareById(long shareId) {
		// TODO Auto-generated method stub
		return deptShareDao.findOne(shareId);
	}

	public List<DeptShare> findAllDeptShareOfOneUser(long userId) {
		return deptShareDao.findAllDeptShareOfOneUser(userId);
	}
	
	public void deleteOneUserAllShare(long userId){
		userShareDao.delete(userShareDao.findOneUserAllUserShare(userId));
		deptShareDao.delete(deptShareDao.findAllDeptShareOfOneUser(userId));
		teamShareDao.delete(teamShareDao.findOneUserAllTeamShare(userId));
		//删除用户所在的其他team
		teamMateDao.delete(teamMateDao.findTeamMateByUserId(userId));
		//删除所有用户创建的team，包含删除team，删除teamShare，删除teamMate
		teamDao.delete(teamDao.listTeamByUserId(userId));
	}*/

	public Page<LinkShare> searchLinkShare(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<LinkShare> spec = Specifications.fromFilters(filters, LinkShare.class);
			return linkShareDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
	
	// ================= user share ===========================
	
	public UserShare getUserShareById(Long id){
		if (id == null){
			return null;
		}
		return userShareDao.findOne(id);
	}
	
	public Page<UserShare> searchUserShare(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<UserShare> spec = Specifications.fromFilters(filters, UserShare.class);
			return userShareDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public UserShare saveUserShare(UserShare userShare) {
		return userShareDao.save(userShare);
	}
	
	public UserShare findUserShareBy(long fileId, long recipientId, long ownerId) {
		return userShareDao.findByFileIdAndOwnerIdAndRecipientId(fileId, ownerId, recipientId);
	}

	public void deleteUserShare(UserShare userShare) {
		userShareDao.delete(userShare);
	}
}
