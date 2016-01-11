//package cn.ac.iscas.oncecloudshare.service.model.common;
//
//import java.util.UUID;
//
//public class TicketHolder {
//
//	// 唯一凭据
//	String ticket;
//	// 最大空闲时间，默认永不过期
//	Long maxIdleTime=Long.MAX_VALUE;
//	// 是否使用一次就失效，默认永不失效
//	boolean touchOnce=false;
//	// 可以凭凭据取到的数据
//	Object principal;
//	// 最后访问时间
//	long lastTouchTime;
//	// 创建时间
//	long createTime;
//
//	public TicketHolder(Object principal, long maxIdleTime, boolean touchOnce){
//		this.ticket=UUID.randomUUID().toString().replaceAll("-","");
//		this.principal=principal==null ? Boolean.TRUE : principal;
//		this.maxIdleTime=maxIdleTime;
//		this.touchOnce=touchOnce;
//		this.lastTouchTime=this.createTime=System.currentTimeMillis();
//	}
//
//	public String getTicket(){
//		return ticket;
//	}
//
//	public void setTicket(String ticket){
//		this.ticket=ticket;
//	}
//
//	public Long getMaxIdleTime(){
//		return maxIdleTime;
//	}
//
//	public void setMaxIdleTime(Long maxIdleTime){
//		this.maxIdleTime=maxIdleTime;
//	}
//
//	public boolean isTouchOnce(){
//		return touchOnce;
//	}
//
//	public void setTouchOnce(boolean touchOnce){
//		this.touchOnce=touchOnce;
//	}
//
//	public Object getPrincipal(){
//		return principal;
//	}
//
//	public void setPrincipal(Object principal){
//		this.principal=principal;
//	}
//
//	public long getLastTouchTime(){
//		return lastTouchTime;
//	}
//
//	public void setLastTouchTime(long lastTouchTime){
//		this.lastTouchTime=lastTouchTime;
//	}
//
//	public long getCreateTime(){
//		return createTime;
//	}
//
//	public void setCreateTime(long createTime){
//		this.createTime=createTime;
//	}
//
//}
