package cn.ac.iscas.oncecloudshare.service.extensions.preview.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.preview.model.Preview;

public interface PreviewDao extends PagingAndSortingRepository<Preview, Long>, JpaSpecificationExecutor<Preview>{
	@Query("from Preview where input = ?1 and converterType = ?2 and converter = ?3")
	Preview findOne(String input, String type, String converter);
	
	@Query("from Preview where input = ?1 and converterType = ?2")
	List<Preview> findAll(String input, String type);
}
