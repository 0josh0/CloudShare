package cn.ac.iscas.oncecloudshare.service.controller.sample;

import cn.ac.iscas.oncecloudshare.service.system.extension.event.Interceptable;

/**
 * 如果是可以打断的event，则需要加上@Interceptable注解
 * 
 * @author Chen Hao
 */
@Interceptable
public class SampleInterceptableEvent extends SampleEvent {

	public SampleInterceptableEvent(String code){
		super(code);
	}

}
