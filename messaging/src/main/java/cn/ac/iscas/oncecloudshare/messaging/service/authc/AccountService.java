package cn.ac.iscas.oncecloudshare.messaging.service.authc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.messaging.model.authc.UserInfo;
import cn.ac.iscas.oncecloudshare.messaging.model.authc.UserInfo.UserStatus;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantTicket;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.service.authc.restclient.RestClient;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class AccountService {
	
	@Autowired
	RestClient restClient;
	
	private LoadingCache<TenantUser,Optional<UserInfo>> userIdCache=CacheBuilder.newBuilder()
			.expireAfterWrite(10,TimeUnit.MINUTES)
			.build(new CacheLoader<TenantUser,Optional<UserInfo>>(){
				
				@Override
				public Optional<UserInfo> load(TenantUser key) throws Exception{
					UserInfo userInfo=restClient.getUserInfo(key.getTenantId(),
							key.getUserId());
					return Optional.fromNullable(userInfo);
				}
	});
	
	private LoadingCache<TenantTicket,Optional<UserInfo>> ticketCache=CacheBuilder.newBuilder()
			.expireAfterWrite(10,TimeUnit.MINUTES)
			.softValues()
			.build(new CacheLoader<TenantTicket,Optional<UserInfo>>(){
				
				@Override
				public Optional<UserInfo> load(TenantTicket key) throws Exception{
					UserInfo userInfo=restClient.getUserInfoByTicket(key.getTenantId(),
							key.getTicket());
					return Optional.fromNullable(userInfo);
				}
	});
	
	private UserInfo checkValid(UserInfo userInfo){
		if(userInfo==null || userInfo.getStatus()!=UserStatus.ACTIVE){
			return null;
		}
		return userInfo;
	}
	
	@Nullable public UserInfo getUserInfo(@Nullable TenantUser tenantUser){
		if(tenantUser==null){
			return null;
		}
		try{
			UserInfo userInfo=userIdCache.get(tenantUser).orNull();
			return checkValid(userInfo);
		}
		catch(ExecutionException e){
			return null;
		}
	}
	
	@Nullable public UserInfo getUserInfoByTicket(@Nullable TenantTicket tenantTicket){
		try{
			UserInfo userInfo=ticketCache.get(tenantTicket).orNull();
			return checkValid(userInfo);
		}
		catch(ExecutionException e){
			return null;
		}
	}
	
//	public boolean isValidUserInfo(UserInfo info){
//		if(info==null){
//			return false;
//		}
//		return info.getStatus().equals(UserStatus.ACTIVE);
//	}
	
	public boolean verifyUserExists(@Nullable TenantUser tenantUser){
		return getUserInfo(tenantUser)!=null;
	}
	
	public boolean verifyTicket(@Nullable TenantUser tenantUser,@Nullable String ticket){
		if(tenantUser==null || ticket==null){
			return false;
		}
		UserInfo info=getUserInfoByTicket(new TenantTicket(tenantUser.getTenantId(),ticket));
		if(info!=null){
			return info.getId().equals(tenantUser.getUserId());
		}
		else{
			return false;
		}
	}
}
