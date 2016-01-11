package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.UUID;

import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;


public abstract class AbstractPrincipalService implements PrincipalService{

	protected abstract void saveHolder(final String ticket,PrincipalHolder holder);
	
	@Override
	public String storePrincipal(Principal principal,long exipresIn,
			boolean updateOnTouch){
		String ticket = UUID.randomUUID().toString().replaceAll("-", "");
		PrincipalHolder holder=new PrincipalHolder(principal,exipresIn,updateOnTouch);
		saveHolder(ticket,holder);
		return ticket;
	}

	protected static class PrincipalHolder{
		
		Principal principal;
		long expiresIn;
		boolean updateOnTouch;
		long lastTouchTime;
		
		public PrincipalHolder(Principal principal,long expiresIn,boolean updateOnTouch){
			this.principal=principal;
			this.expiresIn=expiresIn;
			this.updateOnTouch=updateOnTouch;
			this.lastTouchTime=System.currentTimeMillis();
		}
	}
}
