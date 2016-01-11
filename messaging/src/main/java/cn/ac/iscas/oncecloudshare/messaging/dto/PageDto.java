package cn.ac.iscas.oncecloudshare.messaging.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.google.common.base.Function;
import com.google.common.collect.Lists;


public class PageDto <E> {
	
	/**
	 * 当前页数
	 */
	public int page;
	
	/**
	 * 每页的大小
	 */
	public int pageSize;
	
	/**
	 * 总页数
	 */
	public int totalPages;
	
	/**
	 * 总元素个数
	 */
	public long totalSize;
	
	/**
	 * 具体元素集合
	 */
	public List<E> entries;

	public static <E> PageDto<E> of(Page<E> page){
		PageDto<E> dto=new PageDto<E>();
		dto.page=page.getNumber();
		dto.pageSize=page.getSize();
		dto.totalPages=page.getTotalPages();
		dto.totalSize=page.getTotalElements();
		dto.entries=page.getContent();
		return dto;
	}
	
	public static <E,F> PageDto<E> of(Page<F> page,Function<F,E> transtormer){
		PageDto<E> dto=new PageDto<E>();
		dto.page=page.getNumber();
		dto.pageSize=page.getSize();
		dto.totalPages=page.getTotalPages();
		dto.totalSize=page.getTotalElements();
		dto.entries=Lists.transform(page.getContent(),transtormer);
		return dto;
	}
}
