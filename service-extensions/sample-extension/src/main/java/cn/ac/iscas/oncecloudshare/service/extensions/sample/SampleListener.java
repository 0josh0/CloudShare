package cn.ac.iscas.oncecloudshare.service.extensions.sample;

import java.util.Random;

import org.springframework.http.HttpStatus;

import cn.ac.iscas.oncecloudshare.service.controller.sample.SampleEvent;
import cn.ac.iscas.oncecloudshare.service.controller.sample.SampleInterceptableEvent;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.ThreadSafeListener;

/**
 * Listener可以是任意的类
 * 
 * @author Chen Hao
 */
public class SampleListener {

	/**
	 * 响应event的方法必须加@SubscibeEvent，并且只有一个参数，
	 * 通过参数的类型类区分响应的Event
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public void listen(SampleEvent event){
		System.out.println("listening event:"+event.code);
		throw new RuntimeException("非interceptable事件，抛出异常也没用");
	}
	
	/**
	 * 一个Listener可以包含多个event处理方法，
	 * 如果方法是线程安全的，请加上@ThreadSafeListener注解
	 * 
	 * @param event
	 */
	@SubscribeEvent
	@ThreadSafeListener
	public void intercept(SampleInterceptableEvent event){
		System.out.println("listening interceptable event:"+event.code);
		if(new Random().nextInt(2)==0){
			throw new RestException(HttpStatus.FORBIDDEN,
					"对于interceptable事件，可以抛出RestException异常，终止request");
		}
	}
	
}
