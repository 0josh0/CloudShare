package cn.ac.iscas.oncecloudshare.service.service.common;

import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 在内存中保存principal，适用于单节点版本
 * 
 * @author Chen Hao
 */
//@Service
public final class InMemoryPrincipalService extends AbstractPrincipalService {

	private static final int MAX_CACHE_SIZE=50000;

	private Cache<String,PrincipalHolder> cache=CacheBuilder.newBuilder()
			// .weakKeys() 不能用weakKeys，否则hash不同
			// .weakValues() 用错了，应该是softValues，不是weak
			.softValues().maximumSize(MAX_CACHE_SIZE).build();

	@Override
	protected void saveHolder(String ticket,PrincipalHolder holder){
		cache.put(ticket,holder);
	}
	
	@Override
	public Principal getPrincipal(String ticket){
		PrincipalHolder holder=cache.getIfPresent(ticket);
		if(holder!=null){
			if(holder.expiresIn+holder.lastTouchTime > 
				System.currentTimeMillis()){
				if(holder.updateOnTouch){
					holder.lastTouchTime=System.currentTimeMillis();
				}
				return holder.principal;
			}
			else{
				cache.invalidate(ticket);
			}
		}
		return null;
	}
	
}
