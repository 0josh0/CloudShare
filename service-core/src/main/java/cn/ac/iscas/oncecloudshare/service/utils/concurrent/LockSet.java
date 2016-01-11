package cn.ac.iscas.oncecloudshare.service.utils.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LockSet <K> {

	Cache<K,Lock> lockCache; 
	
	public LockSet(){
		lockCache=CacheBuilder.newBuilder()
				.softValues()
				.build();
	}
	
	synchronized public Lock getLock(K key){
		Lock lock=lockCache.getIfPresent(key);
		if(lock==null){
			lock=new ReentrantLock();
			lockCache.put(key,lock);
		}
		return lock;
	}

}
