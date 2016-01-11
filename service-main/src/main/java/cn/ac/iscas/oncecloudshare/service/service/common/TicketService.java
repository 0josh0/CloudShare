//package cn.ac.iscas.oncecloudshare.service.service.common;
//
//import java.util.UUID;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.gson.Gson;
//
//@Component
//public class TicketService {
//	
////	private Map<String, TicketHolder> tickets = Maps.newConcurrentMap();
//	
//	private static final int MAX_CACHE_SIZE=50000;
//	
////	private Gson gson=Gsons.defaultGsonNoPrettify();
//	
//	private Cache<String,TicketHolder> tickets=CacheBuilder.newBuilder()
////			.weakKeys() 不能用weakKeys，否则hash不同
////			.weakValues() 用错了，应该是softValues，不是weak
//			.softValues()
//			.maximumSize(MAX_CACHE_SIZE)
//			.build();
//			
//
//	@Deprecated
//	public String generateTicket() {
//		return generateTicket(null, Long.MAX_VALUE, false);
//	}
//
//	@Deprecated
//	public String generateTicket(Object principal) {
//		return generateTicket(principal, Long.MAX_VALUE, false);
//	}
//
//	public String generateTicket(Object principal, long maxIdleTime) {
//		return generateTicket(principal, maxIdleTime, false);
//	}
//
//	@Deprecated
//	public String generateTicket(Object principal, Boolean updateOnTouch) {
//		return generateTicket(principal, Long.MAX_VALUE, updateOnTouch);
//	}
//
//	public String generateTicket(Object principal, long maxIdleTime, Boolean updateOnTouch) {
//		TicketHolder ticketHolder = new TicketHolder(principal, maxIdleTime, updateOnTouch);
//		tickets.put(ticketHolder.ticket, ticketHolder);
//		return ticketHolder.ticket;
//	}
//	
//	public Object getPrincipal(String ticket){
//		TicketHolder holder=tickets.getIfPresent(ticket);
//		if(holder!=null){
//			return holder.principal;
//		}
//		return null;
//	}
//
//	public Object touch(String ticket){
//		TicketHolder holder=tickets.getIfPresent(ticket);
//		if(holder!=null){
//			if(!checkIsValid(holder,true)){
//				return null;
//			}
//			if(holder.updateOnTouch){
//				holder.lastTouchTime=System.currentTimeMillis();
//			}
//
//			return holder.principal;
//		}
//		return null;
//	}
//	
//	public boolean contains(String ticket){
//		return tickets.getIfPresent(ticket)!=null;
//	}
//
//	public void deactive(String ticket) {
//		tickets.invalidate(ticket);
//	}
//	
//	/**
//	 * 每小时清除过期ticket
//	 */
//	@Scheduled(cron="0 3 */1 * * *")
//	public void clearInvalidTickets(){
//		for (TicketHolder holder : tickets.asMap().values()){
//			checkIsValid(holder, true);
//		}
//	}
//
//	protected boolean checkIsValid(TicketHolder ticketHolder, boolean removeIfInvalid) {
//		boolean isValid = true;
//		if (System.currentTimeMillis() - ticketHolder.lastTouchTime > ticketHolder.maxIdleTime) {
//			isValid = false;
//		}
//		if (removeIfInvalid && !isValid) {
//			tickets.invalidate(ticketHolder.ticket);
//		}
//		return isValid;
//	}
//
//	static class TicketHolder {
//		// 唯一凭据
//		String ticket;
//		// 最大空闲时间，默认永不过期
//		Long maxIdleTime = Long.MAX_VALUE;
//		// 是否在touch时更新时间
//		boolean updateOnTouch = false;
//		// 可以凭凭据取到的数据
//		Object principal;
//		// 最后访问时间
//		long lastTouchTime;
//		// 创建时间
//		long createTime;
//
//		public TicketHolder(Object principal, long maxIdleTime, boolean updateOnTouch) {
//			this.ticket = UUID.randomUUID().toString().replaceAll("-", "");
//			this.principal = principal == null ? Boolean.TRUE : principal;
//			this.maxIdleTime = maxIdleTime;
//			this.updateOnTouch = updateOnTouch;
//			this.lastTouchTime = this.createTime = System.currentTimeMillis();
//		}
//	}
//}
