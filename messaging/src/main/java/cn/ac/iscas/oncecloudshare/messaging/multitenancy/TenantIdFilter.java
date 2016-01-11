package cn.ac.iscas.oncecloudshare.messaging.multitenancy;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.ac.iscas.oncecloudshare.messaging.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.messaging.utils.SpringUtil;

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;


public class TenantIdFilter extends OncePerRequestFilter{
	
	private static final String TEANT_ID_PARAM="x-tenant-id";
	
	private List<String> whiteList;
	
	public void setWhiteList(List<String> whiteList){
		this.whiteList=whiteList;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,FilterChain filterChain)
			throws ServletException,IOException{
		if(whiteList.contains(request.getPathInfo())){
			filterChain.doFilter(request,response);
			return;
		}
		
		String tenantIdStr=request.getHeader(TEANT_ID_PARAM);
		if(tenantIdStr==null){
			tenantIdStr=request.getParameter(TEANT_ID_PARAM);
		}
		Long tenantId=Longs.tryParse(Strings.nullToEmpty(tenantIdStr));
		
		TenantService tenantService=SpringUtil.getBean(TenantService.class);
		
		boolean validTenant= tenantId!=null && 
				tenantService.setCurrentTenant(tenantId);
		
		if(!validTenant){
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().write("invalid tenant id");
		}
		else{
			filterChain.doFilter(request,response);
		}
	}
}
