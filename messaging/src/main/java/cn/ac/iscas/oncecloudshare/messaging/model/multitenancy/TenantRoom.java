package cn.ac.iscas.oncecloudshare.messaging.model.multitenancy;

public class TenantRoom {

	private long tenantId;
	private long roomId;

	public TenantRoom(long tenantId, long roomId){
		super();
		this.tenantId=tenantId;
		this.roomId=roomId;
	}

	public long getTenantId(){
		return tenantId;
	}

	public void setTenantId(long tenantId){
		this.tenantId=tenantId;
	}

	public long getRoomId(){
		return roomId;
	}

	public void setRoomId(long roomId){
		this.roomId=roomId;
	}

	@Override
	public int hashCode(){
		final int prime=31;
		int result=1;
		result=prime*result+(int)(roomId^(roomId>>>32));
		result=prime*result+(int)(tenantId^(tenantId>>>32));
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
		TenantRoom other=(TenantRoom)obj;
		if(roomId!=other.roomId)
			return false;
		if(tenantId!=other.tenantId)
			return false;
		return true;
	}

}
