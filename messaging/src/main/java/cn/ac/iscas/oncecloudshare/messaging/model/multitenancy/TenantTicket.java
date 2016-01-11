package cn.ac.iscas.oncecloudshare.messaging.model.multitenancy;

import com.google.common.base.Preconditions;

public class TenantTicket {

	private long tenantId;
	private String ticket;

	public TenantTicket(long tenantId, String ticket){
		this.tenantId=tenantId;
		this.ticket=Preconditions.checkNotNull(ticket);
	}

	public long getTenantId(){
		return tenantId;
	}

	public void setTenantId(long tenantId){
		this.tenantId=tenantId;
	}

	public String getTicket(){
		return ticket;
	}

	public void setTicket(String ticket){
		this.ticket=ticket;
	}

	@Override
	public int hashCode(){
		final int prime=31;
		int result=1;
		result=prime*result+(int)(tenantId^(tenantId>>>32));
		result=prime*result+((ticket==null) ? 0 : ticket.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this==obj)
			return true;
		if(obj==null)
			return false;
		if(getClass()!=obj.getClass())
			return false;
		TenantTicket other=(TenantTicket)obj;
		if(tenantId!=other.tenantId)
			return false;
		if(ticket==null){
			if(other.ticket!=null)
				return false;
		}
		else if(!ticket.equals(other.ticket))
			return false;
		return true;
	}

}
