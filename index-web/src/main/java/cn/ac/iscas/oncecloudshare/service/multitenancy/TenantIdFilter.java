package cn.ac.iscas.oncecloudshare.service.multitenancy;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;

public class TenantIdFilter extends OncePerRequestFilter {

	private static final String TEANT_ID_PARAM = "x-tenant-id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		ConfigService<?> configAll = (ConfigService<?>) SpringUtil.getBean("globalConfigService");
		String tmp = configAll.getConfig(Configs.Keys.WEB_TENANT_WHILTE, "");

		TenantService tenantService = SpringUtil.getBean(TenantService.class);

		if (tmp.indexOf(request.getPathInfo()) != -1) {
			filterChain.doFilter(request, response);

			// Request结束时，清除ThreadLocal里的tenant信息，
			// 不然可能影响到以后的request
			tenantService.clearCurrentTenant();
			return;
		}

		String tenantIdStr = request.getHeader(TEANT_ID_PARAM);
		if (tenantIdStr == null) {
			tenantIdStr = request.getParameter(TEANT_ID_PARAM);
		}
		Long tenantId = Longs.tryParse(Strings.nullToEmpty(tenantIdStr));

		boolean validTenant = tenantId != null && tenantService.setCurrentTenant(tenantId);

		if (!validTenant) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().write("invalid tenant id");
		} else {
			filterChain.doFilter(request, response);
		}
		tenantService.clearCurrentTenant();
	}
}
