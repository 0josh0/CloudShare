package cn.ac.iscas.oncecloudshare.messaging.service.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class TicketToken implements AuthenticationToken {

	private static final long serialVersionUID=1L;

	private String ticket;

	public TicketToken(String ticket){
		this.ticket=ticket;
	}

	@Override
	public Object getPrincipal(){
		return ticket;
	}

	@Override
	public Object getCredentials(){
		return ticket;
	}

	public String getTicket(){
		return ticket;
	}
}