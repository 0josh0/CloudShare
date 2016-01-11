package cn.ac.iscas.oncecloudshare.messaging.utils.jpa;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

public class Specifications {

	public static <T>Specification<T> fromQuery(String query,
			Class<T> entityClass){
		return fromFilters(SearchFilter.parseQuery(query),entityClass);
	}

	public static <T>Specification<T> fromFilters(
			final Collection<SearchFilter> filters, final Class<T> entityClass){
		return new Specification<T>(){

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
					CriteriaBuilder builder){
				if(Collections3.isNotEmpty(filters)){

					List<Predicate> predicates=Lists.newArrayList();
					for(SearchFilter filter: filters){
						// nested path translate, 如Task的名为"user.name"的filedName,
						// 转换为Task.user.name属性
						String[] names=StringUtils.split(filter.fieldName,".");
						Path expression=root.get(names[0]);
						for(int i=1; i<names.length; i++){
							expression=expression.get(names[i]);
						}
						
						Object value=parseData(expression.getJavaType(),filter.value);
						if(value==null){
							predicates.add(builder.isNull(expression));
							continue;
						}

						// logic operator
						switch(filter.operator){
						case EQ:
							predicates.add(builder.equal(expression,
									value));
							break;
						case NE:
							predicates.add(builder.notEqual(expression,value));
							break;
						case LIKE:
							predicates.add(builder.like(expression,value+""));
							break;
						case CONTAINS:
							predicates.add(builder.like(expression,"%"+value+"%"));
							break;
						case GT:
							predicates.add(builder.greaterThan(expression,
									(Comparable)value));
							break;
						case LT:
							predicates.add(builder.lessThan(expression,
									(Comparable)value));
							break;
						case GTE:
							predicates.add(builder.greaterThanOrEqualTo(
									expression,(Comparable)value));
							break;
						case LTE:
							predicates.add(builder.lessThanOrEqualTo(
									expression,(Comparable)value));
							break;
						case ISNULL:
							predicates.add(builder.isNull(expression));
							break;
						}
					}

					// 将所有条件用 and 联合起来
					if(predicates.size()>0){
						return builder.and(predicates
								.toArray(new Predicate[predicates.size()]));
					}
				}

				return builder.conjunction();
			}
		};
	}
	
	private static Object parseData(Class<?> clazz,Object value){
		if(!(value instanceof String)){
			return value;
		}
		String strValue=value.toString();
		if(clazz.equals(String.class)){
			return value;
		}
		if(clazz.isEnum()){
			strValue=strValue.toUpperCase();
		}
		try{
			return Gsons.defaultGson().fromJson(strValue,clazz);
		}
		catch(JsonSyntaxException e){
			return null;
		}
	}
	
}
