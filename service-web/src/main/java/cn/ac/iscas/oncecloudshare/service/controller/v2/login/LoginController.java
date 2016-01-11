package cn.ac.iscas.oncecloudshare.service.controller.v2.login;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.login.LoginResponseDto;
import cn.ac.iscas.oncecloudshare.service.event.login.LoginEvent;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.PrincipalService;
import cn.ac.iscas.oncecloudshare.service.system.extension.login.LoginExtension;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

/**
 * 登录，登出控制层
 * 
 * 
 */
@Controller
@RequestMapping(value = "/api/v2/", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class LoginController extends BaseController {
	
	static final Logger _logger = LoggerFactory.getLogger(LoginController.class);
	private static String DEFAULT_PARAM_SOURCE = "source";
	// 默认登录最大的空闲时间为30分钟
	private static Long DEFAULT_MAX_IDLE_TIME = 60 * DateUtils.MILLIS_PER_MINUTE;
	
	@SuppressWarnings("unused")
	private static final String ATTR_TICKET = "ticket";
	// @Resource
	// private DefaultLoginExtensionManager loginExtensionManager;
	@Resource
	private PrincipalService pService;
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;
	// 用于区分使用哪个登录插件的参数
	private String parmaSource = DEFAULT_PARAM_SOURCE;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String login(HttpServletRequest request, HttpServletResponse response) {
		
		AuthenticationToken token = createToken(request, response);
		if (token == null) {
			_logger.warn("HttpServletRequest{}未生成对应的AuthenticationToken", request.getParameterMap());
			throw new RestException(ErrorCode.UNAUTHORIZED);
		}
		try {
			SecurityUtils.getSubject().login(token);
		} catch (AuthenticationException e) {
			throw new RestException(ErrorCode.UNAUTHORIZED);
		}
		// 判断用户是否被冻结
		if (!UserStatus.ACTIVE.equals(currentUser().getStatus())) {
			throw new RestException(ErrorCode.USER_NOT_ACTIVE, currentUser().getStatus().name());
		}

		// 登录成功
		UserPrincipal up = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();

		String ticket = pService.storePrincipal(up, DEFAULT_MAX_IDLE_TIME, true);

		postEvent(new LoginEvent(request, up, ticket));
		
		LoginResponseDto dto = new LoginResponseDto(ticket,up.getTenantId(),uService.find(up.getUserId()));
		
		return gson().toJson(dto);
	}

	@RequestMapping(value = "/logout")
	@ResponseBody
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if (principal instanceof UserPrincipal) {
			UserPrincipal user = (UserPrincipal) principal;
			// ticketService.deactive((String) user.getAttribute(ATTR_TICKET));
			SecurityUtils.getSubject().logout();
		}
		return gson().toJson(ResponseDto.OK);
	}

	protected AuthenticationToken createToken(HttpServletRequest request, HttpServletResponse response) {
		String source = request.getParameter(parmaSource);
		LoginExtension extension = runtimeContext.getLoginExtensionManager().getExtension(source);
		return extension.createToken(request, response);
	}
}