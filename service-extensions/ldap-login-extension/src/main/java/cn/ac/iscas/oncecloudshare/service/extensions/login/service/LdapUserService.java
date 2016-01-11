package cn.ac.iscas.oncecloudshare.service.extensions.login.service;

import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.extensions.login.dao.LdapUserDao;
import cn.ac.iscas.oncecloudshare.service.extensions.login.model.LdapUser;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthorizationService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.shiro.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;

@Service
@Transactional(readOnly = true)
public class LdapUserService implements AuthorizationService {
	public static final String LDAP_HOST = "extensions.login.ldap.host";
	public static final String LDAP_PORT = "extensions.login.ldap.port";

	private static final Logger _logger = LoggerFactory.getLogger(LdapUserService.class);

	@Resource
	private ConfigService configService;
	@Resource
	private UserService userService;
	@Resource
	private LdapUserDao ldapUserDao;

	private LockSet<String> principalLocks = new LockSet<String>();

	protected String getProviderUrl() {
		String host = configService.getConfig(LDAP_HOST, StringUtils.EMPTY);
		int port = configService.getConfigAsInteger(LDAP_PORT, 389);
		return "ldap://" + host + ":" + port;
	}

	@Override
	public boolean verifyAccountExists(Object principal) {
		return false;
	}

	@Override
	public Object getPrincipal(AuthenticationToken token) {
		String principal = (String) token.getPrincipal();
		String credentials = new String((char[]) token.getCredentials());
		if (verify(principal, credentials)) {
			Lock lock = principalLocks.getLock(principal);
			lock.lock();
			try {
				LdapUser ldapUser = ldapUserDao.findByLdapPrincipal(principal);
				if (ldapUser == null) {
					ldapUser = createLdapUser(principal);
				}
				return new UserPrincipal(ldapUser.getUser());
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

	@Transactional(readOnly = false)
	public LdapUser createLdapUser(String ldapPrincipal) {
		User user = new User();
		user.setName(ldapPrincipal);
		user.setEmail(UUID.randomUUID().toString() + "@ldap.cn");
		user.setPlainPassword(Base64.encodeBase64String(ldapPrincipal.getBytes()).substring(0, 16));
		user.setDepartment(null);
		user.setQuota(configService.getConfigAsLong(Configs.Keys.USER_QUOTA, Configs.Defaults.USER_QUOTA));
		UserStatus status = UserStatus.ACTIVE;
		if (configService.getConfigAsBoolean(Configs.Keys.REG_NEED_APPROVAL, false)) {
			status = UserStatus.APPLYING;
		}
		user.setStatus(status);
		userService.addUser(user);

		LdapUser ldapUser = new LdapUser();
		ldapUser.setLdapPrincipal(ldapPrincipal);
		ldapUser.setUser(user);
		return ldapUserDao.save(ldapUser);
	}

	protected boolean verify(String principal, String credentials) {
		Hashtable<String, String> HashEnv = new Hashtable<String, String>();
		HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		HashEnv.put(Context.SECURITY_PRINCIPAL, principal);
		HashEnv.put(Context.SECURITY_CREDENTIALS, credentials);
		HashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		HashEnv.put(Context.PROVIDER_URL, getProviderUrl());
		try {
			LdapContext ctx = new InitialLdapContext(HashEnv, null);
			ctx.close();
			return true;
		} catch (javax.naming.AuthenticationException e) {
			_logger.debug("ldap验证失败{principal=" + principal + ";credentials=" + credentials + "}", e);
		} catch (NamingException e) {
			_logger.error("ldap验证失败{principal=" + principal + ";credentials=" + credentials + "}", e);
		}
		return false;
	}
}
