//package cn.ac.iscas.oncecloudshare.messaging.service.authc;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.vysper.xmpp.addressing.Entity;
//import org.apache.vysper.xmpp.addressing.EntityImpl;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import cn.ac.iscas.oncecloudshare.messaging.service.authc.restclient.RestClient;
//import cn.ac.iscas.oncecloudshare.messaging.utils.Constants;
//import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
//
//import com.google.common.base.Function;
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//
//@Service(value="uService")
//public class UserService {
////	
////	private static Logger logger=LoggerFactory.getLogger(UserService.class);
////
////	@Autowired
////	RestClient client;
////	
////	Map<Long,String> passwordCache=Maps.newHashMap();
////	
////	LoadingCache<Long,Boolean> existenceCache=CacheBuilder.newBuilder()
////			.expireAfterWrite(10,TimeUnit.MINUTES)
////			.build(new CacheLoader<Long,Boolean>(){
////				
////				@Override
////				public Boolean load(Long key) throws Exception{
//////					return client.verifyUserExists(key);
////					return true;
////				}
////	});
////	
////	public boolean verifyAccountExists(Entity jid){
////		Long userId=JIDUtil.parseUserId(jid);
////		if(userId==null){
////			return false;
////		}
////		return verifyAccountExists(userId);
////	}
////	
////	public boolean verifyAccountExists(long userId){
////		
////		Boolean res=existenceCache.getIfPresent(userId);
////		if(res!=Boolean.TRUE){
////			existenceCache.invalidate(userId);
////			res=existenceCache.getUnchecked(userId);
////		}
////		return res==Boolean.TRUE;
////	}
////	
////	public boolean verifyPassword(Entity jid, String password){
////		Long userId=JIDUtil.parseUserId(jid);
////		if(userId==null){
////			return false;
////		}
////		return verifyPassword(userId,password);
////	}
////	
////	public boolean verifyPassword(long id, String password){
////		String cache=passwordCache.get(id);
////		if(cache!=null){
////			if(cache.equals(password)){
////				return true;
////			}
////		}
//////		boolean correct=client.verifyPassword(id,password);
////		boolean correct=true;
////		if(correct){
////			passwordCache.put(id,password);
////		}
////		return correct;
////	}
//	
////	public List<Long> getAllUserIds(){
//////		return client.getAllUserIds();
////		return null;
////	}
////	
////	public List<Entity> getAllEntities(){
////		final String domain=Constants.domain();
////		return null;
//////		return Lists.transform(client.getAllUserIds(),
//////				new Function<Long,Entity>(){
//////
//////					@Override
//////					public Entity apply(Long input){
//////						return EntityImpl.parseUnchecked(input+"@"+domain);
//////					}
//////
//////				}
//////		);
////	}
//}
