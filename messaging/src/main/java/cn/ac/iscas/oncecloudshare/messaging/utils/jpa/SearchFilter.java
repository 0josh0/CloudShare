package cn.ac.iscas.oncecloudshare.messaging.utils.jpa;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;


public class SearchFilter {

	public static String QUERY_SEPRATOR=",,";
	public static String OPERATOR_SEPRATOR="::";
	
	public enum Operator {
		EQ, NE, GT, LT, GTE, LTE, LIKE, CONTAINS, ISNULL;
		
		public static Operator of(String op){
			for(Operator value:values()){
				if(value.name().equalsIgnoreCase(op)){
					return value;
				}
			}
			return null;
		}
	}

	public String fieldName;
	public Object value;
	public Operator operator;
	
	public SearchFilter(String fieldName, Operator operator, Object value){
		this.fieldName=fieldName;
		this.value=value;
		this.operator=operator;
	}
	
	public static SearchFilter of(String queryUnit){
		Splitter splitter=Splitter.on(OPERATOR_SEPRATOR).omitEmptyStrings().trimResults();
		List<String> list=Lists.newArrayList(splitter.split(queryUnit));
		if(list.size()!=3){
			return null;
		}
		Operator operator=Operator.of(list.get(1));
		if(operator==null){
			return null;
		}
		return new SearchFilter(list.get(0),operator,list.get(2));
	}
	
	public static List<SearchFilter> parseQuery(String query){
		List<SearchFilter> filters=Lists.newArrayList();
		Splitter splitter=Splitter.on(QUERY_SEPRATOR).omitEmptyStrings().trimResults();
		for(String queryUnit:splitter.split(query)){
			SearchFilter filter=SearchFilter.of(queryUnit);
			if(filter!=null){
				filters.add(filter);
			}
		}
		return filters;
	}
}
