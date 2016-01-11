package cn.ac.iscas.oncecloudshare.service.service.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springside.modules.nosql.redis.JedisTemplate;

import redis.clients.jedis.JedisPool;


@Component
public class RedisOperator extends JedisTemplate {

	@SuppressWarnings ("unused")
	private static Logger logger=LoggerFactory.getLogger(RedisOperator.class);

	private static RedisOperator instance;

	public static RedisOperator getInstance(){
		return instance;
	}

	@Autowired(required = true)
	public RedisOperator(JedisPool jedisPool){
		super(jedisPool);
		instance=this;
	}
}
