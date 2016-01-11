package cn.ac.iscas.oncecloudshare.messaging.utils.jpa;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;


public class Pageables {
	
	public static Pageable buildPageable(int page,int pageSize,
			String sortParam,Class<?> entityClass){
		Sort sort=null;
		if(sortParam!=null && entityClass!=null){
			sort=buildSort(sortParam,entityClass);
		}
		return new PageRequest(page,pageSize,sort);
	}

	/**
	 * 通过String参数构造一个Sort对象<br/>
	 * 比如："name,-age"表示先按name升序排列，再按age降序排列
	 * 
	 * @param sortParam
	 * @param clazz
	 * @return
	 */
	public static Sort buildSort(String sortParam,Class<?> clazz){
		List<Order> orders=Lists.newArrayList(); 
		for(String field:Splitter.on(',').omitEmptyStrings()
				.trimResults().split(sortParam)){
			Direction dir=Direction.ASC;
			if(field.startsWith("-")){
				if(field.length()==1){
					continue;
				}
				dir=Direction.DESC;
				field=field.substring(1);
			}
			if(getClassField(clazz,field)!=null){
				orders.add(new Order(dir,field));
			}
		}
		if(orders.isEmpty()){
			return null;
		}
		else{
			return new Sort(orders);
		}
	}
	
	private static Field getClassField(Class<?> clazz,String field){
		try{
			return clazz.getDeclaredField(field);
		}
		catch (Exception e) {
			Class<?> superClass=clazz.getSuperclass();
			if(superClass!=null){
				return getClassField(superClass,field);
			}
			else{
				return null;
			}
		}
	}
}
