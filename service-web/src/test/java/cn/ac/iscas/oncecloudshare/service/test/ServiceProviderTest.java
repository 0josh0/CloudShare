package cn.ac.iscas.oncecloudshare.service.test;

import java.util.Set;

import com.google.common.collect.Sets;

import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;


public class ServiceProviderTest {
	
	private static Set<Class<? extends ServiceProvider>> getServiceProviderInterfaces(Class<?> clazz){
		Set<Class<? extends ServiceProvider>> spInterfaces=Sets.newHashSet();
		
		Class<?> currentClass=clazz;
		while(currentClass!=null){
			for(Class<?> theInterface:currentClass.getInterfaces()){
				for(Class<?> parentInterface:theInterface.getInterfaces()){
					if(parentInterface==ServiceProvider.class){
						spInterfaces.add((Class<? extends ServiceProvider>)theInterface);
					}
				}
			}
			
			currentClass=currentClass.getSuperclass();
		}
		
		return spInterfaces;
	}

	public static void main(String[] args){
		for(Class clazz:getServiceProviderInterfaces(D.class)){
			System.out.println(clazz);
		}
		
		D d=null;
		
		A a=(A)d;
		
		System.out.println(a);
	}
}

interface A extends ServiceProvider{
	
}

interface B extends ServiceProvider{
	
}

class C implements B{
	
}

class D extends C implements A{
	
}
