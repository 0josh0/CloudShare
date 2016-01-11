package cn.ac.iscas.oncecloudshare.messaging.controller;


import java.util.List;

import org.springframework.data.domain.Pageable;

import cn.ac.iscas.oncecloudshare.messaging.utils.jpa.Pageables;

public class PageParam {
	
	public static final int DEFAULT_PAGE_SIZE=20;
	public static final int MAX_PAGE_SIZE=500;

	Integer page=0;
	Integer pageSize=DEFAULT_PAGE_SIZE;
	String sort;
	List<String> fields;
	
	public Pageable getPageable(Class<?> entityClass){
		return Pageables.buildPageable(page,pageSize,sort,entityClass);
	}
	
	public void setSortIfAbsent(String sort){
		if(this.sort==null){
			this.sort=sort;
		}
	}

	public Integer getPage(){
		return page;
	}

	public void setPage(Integer page){
		this.page=Math.max(page,0);
	}

	public Integer getPageSize(){
		return pageSize;
	}

	public void setPageSize(Integer pageSize){
		this.pageSize=Math.min(Math.max(pageSize,1),MAX_PAGE_SIZE);
	}

	public String getSort(){
		return sort;
	}

	public void setSort(String sort){
		this.sort=sort;
	}

	public List<String> getFields(){
		return fields;
	}

	public void setFields(List<String> fields){
		this.fields=fields;
	}

}
