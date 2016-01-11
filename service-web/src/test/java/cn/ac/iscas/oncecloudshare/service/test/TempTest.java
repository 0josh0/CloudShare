package cn.ac.iscas.oncecloudshare.service.test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class TempTest {

	public static void main(String[] args){
		
//		System.out.println(System.identityHashCode(1L));
//		System.out.println(System.identityHashCode(new Long(1L)));
		
		Cache<String,Long> tickets=CacheBuilder.newBuilder()
				.weakKeys()
				.weakValues()
				.maximumSize(50000)
				.build();
		tickets.put("hehe",1L);
		System.out.println(tickets.getIfPresent(new String("hehe")));
	}
}
