package cn.ac.iscas.oncecloudshare.messaging.utils;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityFormatException;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantRoom;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import com.google.common.primitives.Longs;


public class JIDUtil {
	
	private static Logger logger=LoggerFactory.getLogger(JIDUtil.class);
	
	private static final String TENANT_ID_SEPRATOR="-";

//	private static class LongPair{
//		public Long _1;
//		public Long _2;
//		
//		public static LongPair of(Long _1,Long _2){
//			LongPair pair=new LongPair();
//			pair._1=_1;
//			pair._2=_2;
//			return pair;
//		}
//	}
	
	private static TenantUser toTenantUser(String str){
		String[] arr=StringUtils.split(str,TENANT_ID_SEPRATOR);
		if(arr.length==2){
			Long a=Longs.tryParse(arr[0]);
			Long b=Longs.tryParse(arr[1]);
			if(a!=null && b!=null){
				return new TenantUser(a,b);
			}
		}
		return null;
	}
	
	@Nullable public static TenantUser parseTenantUser(Entity jid){
		//filter other domains
		String domain=Constants.domain();
		if(jid.getDomain().equals(domain)==false){
			return null;
		}
		return toTenantUser(jid.getNode());
	}
	
	@Nullable public static TenantUser parseTenantUser(String bareJid){
		try{
			return parseTenantUser(EntityImpl.parseUnchecked(bareJid));
		}
		catch(Exception e){
			return null;
		}
	}
	
//	@Nullable public static Long parseUserId(Entity jid){
//		TenantUser user=parseTenantUser(jid);
//		if(user!=null){
//			return user.getUserId();
//		}
//		else{
//			return null;
//		}
//	}
//	
//	@Nullable public static Long parseUserId(String bareJid){
//		try{
//			return parseUserId(EntityImpl.parseUnchecked(bareJid));
//		}
//		catch(Exception e){
//			return null;
//		}
//	}
	
	public static Entity buildEntity(long tenantId,long userId){
		String node=tenantId+TENANT_ID_SEPRATOR+userId;
		return EntityImpl.parseUnchecked(node+"@"+Constants.domain());
	}
	
	@Nullable public static Entity tryParseEntity(String jid){
		try{
			return EntityImpl.parse(jid);
		}
		catch(EntityFormatException e){
			return null;
		}
	}
	
//	public static boolean isValidEntity(Entity entity){
//		return isNotifEntity(entity) || isValidUserEntity(entity);
//	}
	
	public static boolean isNotifEntity(Entity entity){
		if(entity==null){
			return false;
		}
		return entity.getNode().equals(Constants.notifUsername());
	}
	
//	public static boolean isValidUserEntity(Entity entity){
//		if(entity==null){
//			return false;
//		}
//		Long userId=parseUserId(entity);
//		return verifyUserIdExists(userId);
//	}
	
//	public static boolean verifyUserIdExists(Long userId){
//		if(userId==null){
//			return false;
//		}
//		UserService uService=SpringUtil.getBean(UserService.class);
//		return uService.verifyAccountExists(userId);
//	}
	
	private static TenantRoom toTenantRoom(String str){
		String[] arr=StringUtils.split(str,TENANT_ID_SEPRATOR);
		if(arr.length==2){
			Long a=Longs.tryParse(arr[0]);
			Long b=Longs.tryParse(arr[1]);
			if(a!=null && b!=null){
				return new TenantRoom(a,b);
			}
		}
		return null;
	}
	
	public static TenantRoom parseTenantRoom(Entity roomJid){
		String domain=Constants.mucSubdomain();
		if(roomJid.getDomain().equals(domain)==false){
			return null;
		}
		return toTenantRoom(roomJid.getNode());
	}
	
//	public static Long parseRoomId(Entity roomJid){
//		TenantRoom room=parseTenantRoom(roomJid);
//		return room!=null? room.getRoomId():null;
//	}
	
	public static Entity buildRoomEntity(Long tenantId,long roomId){
		String node=tenantId+TENANT_ID_SEPRATOR+roomId;
		return new EntityImpl(node,Constants.mucSubdomain(),null);
	}
	
//	@Deprecated
//	public static boolean verifyRoomIdExists(Long roomId){
//		if(roomId==null){
//			return false;
//		}
//		RoomService rService=SpringUtil.getBean(RoomService.class);
//		return rService.findOne(roomId)!=null;
//	}
	
	public static TenantUser parseRoomUser(Entity entity){
		return toTenantUser(entity.getResource());
	}
	
//	public static Entity buildOccupantEntity(long roomId,long userId){
//		return new EntityImpl(roomId+"",Constants.mucSubdomain(),userId+"");
//	}
}
