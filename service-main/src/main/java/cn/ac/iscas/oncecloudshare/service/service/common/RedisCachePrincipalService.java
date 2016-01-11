package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.nosql.redis.JedisTemplate.JedisAction;
import org.springside.modules.nosql.redis.JedisTemplate.JedisActionNoResult;

import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.Principal;
import cn.ac.iscas.oncecloudshare.service.service.redis.RedisOperator;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;


/**
 * 使用redis保存principal，适用于集群版本
 *
 * @author Chen Hao
 */
@Service
public class RedisCachePrincipalService extends AbstractPrincipalService{
	
	private static final Logger logger=LoggerFactory.getLogger(RedisCachePrincipalService.class);
	
	private static final String FIELD_CLASS="c";
	private static final String FIELD_PRINCIPAL="p";
	private static final String FIELD_EXPIRES_IN="ei";
//	private static final String FIELD_LAST_TOUCH_TIME="ltt";
	private static final String FIELD_UPDATE_ON_TOUCH="uot";

	@Autowired
	RedisOperator redisOperator;
	
	Gson gson=Gsons.defaultGsonNoPrettify();
	
	@Override
	public Principal getPrincipal(final String ticket){
		List<String> res=redisOperator.execute(new JedisAction<List<String>>(){

			@Override
			public List<String> action(Jedis jedis){
				List<String> res=jedis.hmget(ticket,FIELD_CLASS,FIELD_PRINCIPAL,
						FIELD_EXPIRES_IN,FIELD_UPDATE_ON_TOUCH);
				if(res==null || res.contains(null)){
					return null;
				}
				if(Boolean.valueOf(res.get(3))){
					long expire=Longs.tryParse(res.get(2));
					jedis.expire(ticket,(int)(expire/1000));
				}
				return res;
			}
		});
		if(res!=null){
			try{
				Class<?> clazz=Class.forName(res.get(0));
				return (Principal)gson.fromJson(res.get(1),clazz);
			}
			catch(Exception e){
				logger.error("",e);
			}
		}
		return null;
	}

	@Override
	protected void saveHolder(final String ticket, final PrincipalHolder holder){
		redisOperator.execute(new JedisActionNoResult(){
			
			@Override
			public void action(Jedis jedis){
				Map<String,String> map=Maps.newHashMapWithExpectedSize(3);
				map.put(FIELD_CLASS,holder.principal.getClass().getCanonicalName());
				map.put(FIELD_PRINCIPAL,gson.toJson(holder.principal));
				map.put(FIELD_EXPIRES_IN,String.valueOf(holder.expiresIn));
				map.put(FIELD_UPDATE_ON_TOUCH,String.valueOf(holder.updateOnTouch));
				jedis.hmset(ticket,map);
				jedis.expire(ticket,(int)(holder.expiresIn/1000));
			}
		});
		
	}

	
	
}
