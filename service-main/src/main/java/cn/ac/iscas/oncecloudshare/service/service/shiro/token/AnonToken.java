package cn.ac.iscas.oncecloudshare.service.service.shiro.token;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;

public class AnonToken implements AuthenticationToken {
	private static final long serialVersionUID = 1L;
	
	private static AnonToken INSTANCE=new AnonToken(); 
	
	private AnonToken() {
    }
	
	public static AnonToken of(){
		return INSTANCE;
	}
	
	@Override
	public Object getPrincipal(){
		return StringUtils.EMPTY;
	}

	@Override
	public Object getCredentials(){
		return StringUtils.EMPTY;
	}
}