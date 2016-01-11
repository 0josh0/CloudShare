package cn.ac.iscas.oncecloudshare.messaging.service.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;

import cn.ac.iscas.oncecloudshare.messaging.utils.Constants;


public class AdminFilter extends AccessControlFilter{
	
	private String secretKey;

	public AdminFilter(String secretKey){
		this.secretKey=secretKey;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response,Object mappedValue) throws Exception{
		return secretKey.equals(
				WebUtils.toHttp(request).getHeader("x-msg-secret-key"));
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception{
		HttpServletResponse resp=WebUtils.toHttp(response);
		resp.setStatus(HttpStatus.UNAUTHORIZED.value());
//		resp.getWriter().write("");
		return false;
	}

}
