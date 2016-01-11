package cn.ac.iscas.oncecloudshare.service.utils.jpa;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;

public class Specifications {

	public static <T> Specification<T> fromQuery(String query, Class<T> entityClass) {
		return fromFilters(SearchFilter.parseQuery(query), entityClass);
	}

	public static <T> Specification<T> fromFilters(final Collection<SearchFilter> filters, final Class<T> entityClass) {
		return new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				query.distinct(true);
				return and(builder, toPredictes(filters, entityClass, root, query, builder));
			}
		};
	}

	public static <T> Specification<T> disjunction(final Collection<SearchFilter> filters, final Class<T> entityClass) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				query.distinct(true);
				return or(builder, toPredictes(filters, entityClass, root, query, builder));
			}
		};
	}

	public static <T> Specification<T> fromFilters(final Collection<SearchFilter> andFilters, final Collection<SearchFilter> orFilters,
			final Class<T> entityClass) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				query.distinct(true);
				Predicate andPredicate = and(builder, toPredictes(andFilters, entityClass, root, query, builder));
				Predicate orPredicate = or(builder, toPredictes(orFilters, entityClass, root, query, builder));
				return builder.and(andPredicate, orPredicate);
			}
		};
	}

	protected static Predicate and(CriteriaBuilder builder, List<Predicate> predicates) {
		if (Collections3.isEmpty(predicates)) {
			return builder.conjunction();
		}
		return builder.and(predicates.toArray(new Predicate[predicates.size()]));
	}

	protected static Predicate or(CriteriaBuilder builder, List<Predicate> predicates) {
		if (Collections3.isEmpty(predicates)) {
			return builder.conjunction();
		}
		return builder.or(predicates.toArray(new Predicate[predicates.size()]));
	}

	protected static <T> List<Predicate> toPredictes(Collection<SearchFilter> filters, Class<T> entityClass, final Root<T> root, CriteriaQuery<?> query,
			CriteriaBuilder builder) {
		List<Predicate> predicates = Lists.newArrayList();
		if (Collections3.isEmpty(filters)) {
			return predicates;
		}
		final Map<String, Join<?, ?>> joins = Maps.newHashMap();
		for (SearchFilter filter : filters) {
			Path expression;
			int index = filter.fieldName.lastIndexOf('.');
			if (index == -1){
				expression = root.get(filter.fieldName);
			} else {
				Function<String, Join<?, ?>> function = new Function<String, Join<?, ?>>() {
					public Join<?, ?> apply(String path){
						Join<?, ?> join = joins.get(path);
						if (join == null){
							int index = path.indexOf('.');
							if (index == -1){
								join = root.join(path);
							} else {
								join = apply(path.substring(0, index)).join(path.substring(index + 1));
							}
							joins.put(path, join);
						}
						return join;
					}
				};
				expression = function.apply(filter.fieldName.substring(0, index)).get(filter.fieldName.substring(index + 1));
			}

			Object value = parseData(expression.getJavaType(), filter.value);
			if (value == null) {
				predicates.add(builder.isNull(expression));
				continue;
			}

			// logic operator
			switch (filter.operator) {
			case EQ:
				predicates.add(builder.equal(expression, value));
				break;
			case NE:
				predicates.add(builder.notEqual(expression, value));
				break;
			case LIKE:
				predicates.add(builder.like(expression, value + ""));
				break;
			case CONTAINS:
				predicates.add(builder.like(expression, "%" + value + "%"));
				break;
			case GT:
				predicates.add(builder.greaterThan(expression, (Comparable) value));
				break;
			case LT:
				predicates.add(builder.lessThan(expression, (Comparable) value));
				break;
			case GTE:
				predicates.add(builder.greaterThanOrEqualTo(expression, (Comparable) value));
				break;
			case LTE:
				predicates.add(builder.lessThanOrEqualTo(expression, (Comparable) value));
				break;
			case ISNULL:
				predicates.add(builder.isNull(expression));
				break;
			case IN:
				if (value.getClass().isArray()){
					predicates.add(expression.in(toArray(value)));
				}
				break;
			}
		}
		return predicates;
	}
	
	private static Object[] toArray(Object obj){
		int length = Array.getLength(obj);
		Object[] array = new Object[length];
		for (int i = 0; i < length; i++){
			array[i] = Array.get(obj, i);
		}
		return array;
	}

	private static Object parseData(Class<?> clazz, Object value) {
		if (!(value instanceof String)) {
			return value;
		}
		String strValue = value.toString();
		if (clazz.equals(String.class)) {
			return value;
		}
		if (clazz.isEnum()) {
			strValue = strValue.toUpperCase();
		}
		try {
			return Gsons.defaultGson().fromJson(strValue, clazz);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

}
