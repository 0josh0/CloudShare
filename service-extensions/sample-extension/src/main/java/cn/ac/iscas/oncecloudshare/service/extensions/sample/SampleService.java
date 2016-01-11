package cn.ac.iscas.oncecloudshare.service.extensions.sample;

import org.springframework.stereotype.Service;

/**
 * 扩展中可以自定义service或者其他component
 * @author Chen Hao
 */
@Service
public class SampleService {

	
	public SampleService(){
		System.out.println("SampleService: I'm loaded.");
	}
	
	public String hello(){
		return "hehe";
	}
}
