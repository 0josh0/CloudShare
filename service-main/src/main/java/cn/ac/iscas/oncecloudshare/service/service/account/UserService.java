package cn.ac.iscas.oncecloudshare.service.service.account;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.validation.Validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.beanvalidator.BeanValidators;
import org.springside.modules.utils.Encodes;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.UserDao;
import cn.ac.iscas.oncecloudshare.service.dao.authorization.UserProfileDao;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.exceptions.account.DuplicateEmailException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InsufficientQuotaException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import cn.ac.iscas.oncecloudshare.service.model.account.Role;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserProfile;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail.EmailParameter;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FolderService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.GlobalUserService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.Digests;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

@Service
@Transactional
public class UserService {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	RuntimeContext runtimeContext;

	@Autowired
	UserDao userDao;

	@Autowired
	UserProfileDao upDao;

	@Autowired
	FolderService folderService;

	@Autowired
	GlobalUserService globalUserService;

	@Autowired
	TenantService tenantService;

	@Autowired
	Validator validator;

	@Resource(name = "globalConfigService")
	private ConfigService<?> configService;

	@Resource
	private MailService mailService;

	private LockSet<Long> modifyLocks = new LockSet<Long>();

	public Page<User> search(List<SearchFilter> filters, Pageable pageable) {
		try {
			filters.add(new SearchFilter("status", Operator.NE, UserStatus.DELETED));
			Specification<User> spec = Specifications.fromFilters(filters, User.class);
			return userDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public User find(Long userId) {
		User user = userDao.findOne(userId);
		return user == null || user.getStatus() == UserStatus.DELETED ? null : user;
	}

	public User findExistingUser(Long id) {
		User user = find(id);
		Preconditions.checkArgument(user != null, "user " + id + " not exists");
		return user;
	}

	public User findByEmail(String email) {
		return userDao.findByEmail(email);
	}

	public Page<User> findAll(Pageable pageable) {
		return search(new ArrayList<SearchFilter>(), pageable);
	}

	public Page<User> findByDepartment(long departmentId, Pageable pageable) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("department.id", Operator.EQ, departmentId));
		return search(filters, pageable);
	}

	/**
	 * 查询某个部门下面的所有用户（包括子部门的用户）
	 * 
	 * @param department
	 * @param status
	 * @return
	 */
	public List<User> findAllByDeparmtent(Department department, UserStatus status) {
		try {
			List<SearchFilter> filters = Lists.newArrayList();
			filters.add(new SearchFilter("department.route", Operator.LIKE, department.getRoute() + "%"));
			filters.add(new SearchFilter("status", Operator.EQ, status));
			Specification<User> spec = Specifications.fromFilters(filters, User.class);
			return userDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public void addUser(User user) {

		canAddNewUser();
		if (userDao.findByEmail(user.getEmail()) != null) {
			throw new DuplicateEmailException("duplicate email: " + user.getEmail());
		}
		if (globalUserService.add(user.getEmail())) {
			user.setId(null);
			user.setRestQuota(user.getQuota());
			if (user.getStatus() == null) {
				user.setStatus(UserStatus.ACTIVE);
			}
			if (!Strings.isNullOrEmpty(user.getPlainPassword())) {
				byte[] salt = PasswordEncryptor.genrateSalt();
				user.setSalt(Encodes.encodeHex(salt));
				user.setPassword(PasswordEncryptor.encriptPassword(user.getPlainPassword(), salt));
			}
			userDao.save(user);

			UserProfile up = new UserProfile();
			up.setUser(user);
			upDao.save(up);

			folderService.makeBuildinFolders(user);
		}
	}

	/**
	 * 验证人数是否超过限定
	 * 
	 * @param tenantId
	 */
	public void canAddNewUser() {

		Tenant tenant = tenantService.getCurrentTenant();

		long limitMembers = tenant.getMembers();

		long trueUsers = userDao.count();

		if (trueUsers  == limitMembers)

			throw new RestException(ErrorCode.CREATE_USER_ERROR);

	}

	/**
	 * 租户正常使用人数
	 * 
	 * @return
	 */
	public long countActiveUsers() {
		ArrayList<SearchFilter> filters = Lists.newArrayList();

		filters.add(new SearchFilter("status", Operator.NE, UserStatus.DELETED));

		Specification<User> spec = Specifications.fromFilters(filters, User.class);

		return userDao.count(spec);
	}

	/**
	 * 设置用户签名
	 * 
	 * @param signature
	 * @param userId
	 */
	public void saveUserSignature(String signature, long userId) {
		userDao.setSignature(signature, userId);
	}

	public void batchAddUser(List<User> list) {
		for (User user : list) {
			addUser(user);
		}
	}

	public boolean verifyPassword(User user, String password) {
		String passwordHash = PasswordEncryptor.encriptPassword(String.valueOf(password), Encodes.decodeHex(user.getSalt()));
		return passwordHash.equals(user.getPassword());
	}

	/**
	 * 租户的用户激活
	 * 
	 * @param id
	 * @param password
	 * @param name
	 */
	public void activate(long id, String password, String name) {
		User user = findExistingUser(id);
		updateUser(id, name, password, null, user.getDepartment(), UserStatus.ACTIVE);
	}

	public void changePassword(long id, String newPassword) {
		User user = findExistingUser(id);
		updateUser(id, null, newPassword, null, user.getDepartment(), null);
	}

	/**
	 * 增加restQuota
	 * 
	 * @param userId
	 *            用户id
	 * @param increment
	 *            增加量（可以为负数）
	 * @throws InsufficientQuotaException
	 *             配额不足
	 */
	public void incrRestQuota(User user, long increment) throws InsufficientQuotaException {
		Lock lock = modifyLocks.getLock(user.getId());
		try {
			lock.lock();

			if (user.getRestQuota() + increment < 0) {
				throw new InsufficientQuotaException();
			}
			int res = userDao.incrRestQuota(user.getId(), increment);
			if (res == 0) {
				throw new InsufficientQuotaException();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 取消用户和部门的关联（将deptIdList部门成员的department设为null）
	 * 
	 * @param deptIdList
	 */
	public void detachDeparment(List<Long> deptIdList) {
		userDao.detachDepartment(deptIdList);
	}

	/**
	 * 更新用户信息
	 * 
	 * @param id
	 * @param name
	 *            如果不为null，将更新此字段
	 * @param password
	 *            如果不为null，将更新此字段
	 * @param quota
	 *            如果不为null，将更新此字段
	 * @param department
	 *            无论是否为null，都将更新此字段
	 * @param status
	 *            如果不为null，将更新此字段
	 */
	public User updateUser(long id, String name, String plainPassword, Long quota, Department department, UserStatus status) {
		Lock lock = modifyLocks.getLock(id);
		try {
			lock.lock();

			User user = findExistingUser(id);
			if (name != null) {
				user.setName(name);
			}
			if (plainPassword != null) {
				user.setPlainPassword(plainPassword);
				String passwordHash = PasswordEncryptor.encriptPassword(String.valueOf(plainPassword), Encodes.decodeHex(user.getSalt()));
				user.setPassword(passwordHash);
			}
			if (quota != null) {
				long increment = quota - user.getQuota();
				long newRestQuota = user.getRestQuota() + increment;
				user.setRestQuota(newRestQuota);
				user.setQuota(quota);
			}
			user.setDepartment(department);
			if (status != null && status != UserStatus.DELETED) {
				user.setStatus(status);
			}
			BeanValidators.validateWithException(validator, user);
			userDao.save(user);
			return user;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 给要删除的user的email加上后缀，防止再注册时email重复
	 * 
	 * @param user
	 * @return
	 */
	private String generateDeletedEmailSuffix(User user) {
		String random = RandomStringUtils.randomAlphanumeric(4);
		return ".del." + random;
	}

	public void deleteUser(long id) {
		User user = findExistingUser(id);

		if (globalUserService.delete(user.getEmail()) == false) {
			throw new RuntimeException("cannot delete global user.");
		}

		user.setEmail(user.getEmail() + generateDeletedEmailSuffix(user));
		user.setStatus(UserStatus.DELETED);
		userDao.save(user);
	}

	// ===== profile

	// public UserProfile findProfile(User user){
	// UserProfile up=upDao.findByUserId(user.getId());
	// if(up==null){
	// up=new UserProfile();
	// up.setUser(user);
	// upDao.save(up);
	// }
	// return up;
	// }

	public UserProfile updateProfile(User user, UserProfile newProfile) {
		UserProfile oldProfile = user.getProfile();
		if (newProfile.getMale() != null) {
			oldProfile.setMale(newProfile.getMale());
		}
		if (newProfile.getWechatAccount() != null) {
			oldProfile.setWechatAccount(newProfile.getWechatAccount());
		}
		if (newProfile.getTel() != null) {
			oldProfile.setTel(newProfile.getTel());
		}
		if (newProfile.getWorkTel() != null) {
			oldProfile.setWorkTel(newProfile.getWorkTel());
		}
		upDao.save(oldProfile);
		return oldProfile;
	}

	AvatarHelper avatarHelper = new AvatarHelper();

	public AvatarHelper getAvatarHelper() {
		return avatarHelper;
	}

	// // ==== Authorization Service
	// @Override
	// public boolean verifyAccountExists(Object principal) {
	// if (principal != null) {
	// return findByEmail(principal.toString()) != null;
	// }
	// return false;
	// }
	//
	// @Override
	// public Object getPrincipal(AuthenticationToken token) {
	// if (token != null && token instanceof UsernamePasswordToken) {
	// UsernamePasswordToken upToken = (UsernamePasswordToken) token;
	// Long tenantId = globalUserService.findTenantId(upToken.getUsername());
	// tenantService.setCurrentTenant(tenantId);
	// User user = findByEmail(upToken.getUsername());
	// if (verifyPassword(user, String.valueOf(upToken.getPassword()))) {
	// return new UserPrincipal(user, tenantId);
	// }
	// }
	// return null;
	// }

	/*
	 * Password
	 */

	private static class PasswordEncryptor {

		public static int SALT_SIZE = 8;
		public static int ITERATION_TIME = 1024;

		static byte[] genrateSalt() {
			byte[] bytes = new byte[SALT_SIZE];
			new Random().nextBytes(bytes);
			return bytes;
		}

		static String encriptPassword(String plainPassword, byte[] salt) {
			byte[] password = Digests.sha1(plainPassword.getBytes(), salt, ITERATION_TIME);
			return Encodes.encodeHex(password);
		}
	}

	/*
	 * Avatar
	 */

	public class AvatarHelper {
		private static final String AVATAR_FORMAT = "png";
		private static final int AVATAR_SIZE_LARGE = 256;
		private static final int AVATAR_SIZE_MIDDLE = 96;
		private static final int AVATAR_SIZE_SMALL = 48;

		public void updateAvatar(User user, ByteSource imgData) throws IOException {
			BufferedImage img = ImageIO.read(imgData.openStream());
			UserProfile profile = user.getProfile();
			profile.setLargeAvatar(saveAvatar(Scalr.resize(img, AVATAR_SIZE_LARGE)));
			profile.setMiddleAvatar(saveAvatar(Scalr.resize(img, AVATAR_SIZE_MIDDLE)));
			profile.setSmallAvatar(saveAvatar(Scalr.resize(img, AVATAR_SIZE_SMALL)));
			upDao.save(profile);
		}

		private String saveAvatar(final BufferedImage img) throws IOException {
			ByteSource source = new ByteSource() {

				@Override
				public InputStream openStream() throws IOException {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ImageIO.write(img, AVATAR_FORMAT, out);
					return new ByteArrayInputStream(out.toByteArray());
				}
			};
			return runtimeContext.getFileStorageService().saveFile(source).getMd5();
		}

		public String getLargeAvatarMd5(User user) throws IOException {
			return user.getProfile().getLargeAvatar();
		}

		public String getMiddleAvatarMd5(User user) throws IOException {
			return user.getProfile().getMiddleAvatar();
		}

		public String getSmallAvatarMd5(User user) throws IOException {
			return user.getProfile().getSmallAvatar();
		}

		public ByteSource getLargeAvatar(User user) throws IOException {
			String md5 = user.getProfile().getLargeAvatar();
			return runtimeContext.getFileStorageService().retrieveFileContent(md5);
		}

		public ByteSource getMiddleAvatar(User user) throws IOException {
			String md5 = user.getProfile().getMiddleAvatar();
			return runtimeContext.getFileStorageService().retrieveFileContent(md5);
		}

		public ByteSource getSmallAvatar(User user) throws IOException {
			String md5 = user.getProfile().getSmallAvatar();
			return runtimeContext.getFileStorageService().retrieveFileContent(md5);
		}
	}
	
	public List<User> findAll(List<SearchFilter> filters) {
		try {
			filters.add(new SearchFilter("status", Operator.NE, UserStatus.DELETED));
			Specification<User> spec = Specifications.fromFilters(filters, User.class);
			return userDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 通过过滤条件查询对应的用户
	 * 
	 * @param and
	 * @param or
	 * @param pageable
	 * @return
	 */
	public Page<User> findAll(List<SearchFilter> and, List<SearchFilter> or, Pageable pageable) {
		try {
			and.add(new SearchFilter("status", Operator.NE, UserStatus.DELETED));
			Specification<User> spec = Specifications.fromFilters(and, or, User.class);
			return userDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public void addRole(User user, Role role) {
		if (user.addRole(role)) {
			userDao.save(user);
		}
	}

	public void deleteRole(User user, Role role) {
		if (user.deleteRole(role)) {
			userDao.save(user);
		}
	}

	/**
	 * 向用户发送注册通过邮件
	 * 
	 * @param user
	 */
	public void sendRegistrationMail(User user) {
		Boolean send = configService.getConfigAsBoolean(Configs.Keys.REG_SEND_MAIL, false);
		if (send) {
			String webUrl = configService.getConfig(Configs.Keys.CLIENT_WEB_URL, "");
			List<EmailParameter> parameters = Lists.newArrayList(new EmailParameter("email", user.getEmail()), new EmailParameter(
					"username", user.getName()), new EmailParameter("web_url", webUrl));

			String subject = configService.getConfig(Configs.Keys.MAIL_REG_SUBJECT, "");
			String content = configService.getConfig(Configs.Keys.MAIL_REG_CONTENT, "");
			Mail mail = new Mail(subject, content, parameters);
			mailService.send(user.getEmail(), mail);
		}
	}

	/**
	 * 向用户发送审核信息邮件
	 * 
	 * @author one
	 * @param user
	 *            注册用户
	 */
	public void sendUserAduitMail(User user) {
		List<EmailParameter> parameters = Lists.newArrayList(new EmailParameter("email", user.getEmail()), new EmailParameter("username",
				user.getName()));

		String subject = configService.getConfig(Configs.Keys.MAIL_AUDIT_SUBJECT, "");
		String content = configService.getConfig(Configs.Keys.MAIL_AUDIT_CONTENT, "");
		Mail mail = new Mail(subject, content, parameters);
		mailService.send(user.getEmail(), mail);

		System.out.println(mailService);
	}
}
