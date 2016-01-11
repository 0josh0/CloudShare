package cn.ac.iscas.oncecloudshare.messaging.model.multitenancy;

public class TenantUser {

	private long tenantId;
	private long userId;

	public TenantUser(long tenantId, long userId){
		super();
		this.tenantId=tenantId;
		this.userId=userId;
	}

	public long getTenantId(){
		return tenantId;
	}

	public void setTenantId(long tenantId){
		this.tenantId=tenantId;
	}

	public long getUserId(){
		return userId;
	}

	public void setUserId(long userId){
		this.userId=userId;
	}

	@Override
	public int hashCode(){
		final int prime=31;
		int result=1;
		result=prime*result+(int)(tenantId^(tenantId>>>32));
		result=prime*result+(int)(userId^(userId>>>32));
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
		TenantUser other=(TenantUser)obj;
		if(tenantId!=other.tenantId)
			return false;
		if(userId!=other.userId)
			return false;
		return true;
	}

}
