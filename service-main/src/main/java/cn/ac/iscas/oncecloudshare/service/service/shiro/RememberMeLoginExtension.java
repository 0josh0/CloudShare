package cn.ac.iscas.oncecloudshare.service.service.shiro;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.event.login.LoginEvent;
import cn.ac.iscas.oncecloudshare.service.service.account.RememberMeService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.AuthenticationService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.collect.ImmutableSet;

@Component
public class RememberMeLoginExtension extends AbstractLoginExtension implements ListenerExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeLoginExtension.class);
	@Resource
	private RuntimeContext runtimeContext;
	@Resource
	private RememberMeService rememberMeService;

	@Override
	public AuthenticationService getAuthorizationService() {
		return rememberMeService;
	}

	@PostConstruct
	public void init() {
		runtimeContext.getExtensionManager().loadExtension(getName(), "0.01", "web端自动登录插件", this);
	}

	@Override
	public String getName() {
		return "rememberme_login_extension";
	}

	@Override
	protected AuthenticationToken createInternalToken(HttpServletRequest request, HttpServletResponse response) {
		String[] authInfo = getBasicPrincipalsAndCredentials(request);
		if (authInfo == null) {
			return new UsernamePasswordToken("", "", isRememberMe(request), getRequestHost(request));
		}
		return new UsernamePasswordToken(authInfo[0], authInfo[1], false, getRequestHost(request));
	}

	@Override
	public Set<Object> getListeners() {
		return ImmutableSet.<Object> of(this);
	}

	@SubscribeEvent
	public void handleLogin(LoginEvent event) {
		try {
			if (isRememberMe(event.getRequest()) && !getName().equals(SpringUtil.getParamOrHeader("source"))) {
				UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
				String ticket = event.getTicket();
				rememberMeService.remember(principal.getUserId(), ticket);
			}
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
	}
}