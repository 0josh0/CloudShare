package cn.ac.iscas.oncecloudshare.service.service.account;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.RememberMeDao;
import cn.ac.iscas.oncecloudshare.service.model.account.RememberMe;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.GlobalConfig;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.TenantStatus;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;

@Service
@Transactional(readOnly = false)
public class RememberMeService implements AuthenticationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeService.class);

	@Resource
	private RememberMeDao rememberMeDao;
	@Resource
	private TenantService tenantService;
	@Resource
	private UserService userService;
	@Resource(name = "globalConfigService")
	private ConfigService<GlobalConfig> configService;
	@PersistenceContext
	private EntityManager em;

	@Override
	public Object getPrincipal(AuthenticationToken token) {
		String[] ticketAndTenant = ((String)token.getPrincipal()).split("-");
		if (ticketAndTenant.length != 2){
			return null;
		}
		long tenantId = NumberUtils.toLong(ticketAndTenant[1], Long.MIN_VALUE);
		if (tenantService.setCurrentTenantManually(tenantId)){
			RememberMe rememberMe = rememberMeDao.findByToken(ticketAndTenant[0]);
			if (rememberMe != null && rememberMe.getExpireAt().getTime() > System.currentTimeMillis()) {
				return new UserPrincipal(rememberMe.getUser(), tenantService.getCurrentTenant().getId());
			}
		}
		return null;
	}

	public RememberMe remember(Long userId, String ticket) {
		User user = userService.find(userId);
		if (user == null || !UserStatus.ACTIVE.equals(user.getStatus())) {
			return null;
		}
		RememberMe rememberMe = new RememberMe();
		rememberMe.setToken(ticket);
		rememberMe.setUser(user);
		int expireDay = configService.getConfigAsInteger(Configs.Keys.REMEMBERME_EXPIRE_DAY, Configs.Defaults.REMEMBERME_EXPIRE_DAY);
		rememberMe.setExpireAt(DateUtils.addDays(new Date(), expireDay));
		rememberMe = rememberMeDao.save(rememberMe);
		return rememberMe;
	}

	/**
	 * 每天凌晨3点清除过期ticket
	 */
	@Scheduled(cron = "0 0 3 0/1 * *")
	public void deleteExpired() {
		List<Tenant> tenants = tenantService.findAll(null);
		for (Tenant tenant : tenants) {
			if (TenantStatus.NORMAL.equals(tenant.getStatus())){
				try {
					deleteExpired(tenant);
				} catch (Exception e) {
					LOGGER.warn("定时任务清除租户" + tenant.getId() + "过期ticket失败", e);
				}
			}
		}
	}

	public void deleteExpired(Tenant tenant) {
		// 手动切换schema
		String sql = "USE " + Constants.TENANT_SCHEMA_PREFIX + tenant.getId();
		em.createNativeQuery(sql).executeUpdate();
		rememberMeDao.deleteExpired();
	}
}
