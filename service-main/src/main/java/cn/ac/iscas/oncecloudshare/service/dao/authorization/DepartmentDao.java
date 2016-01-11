package cn.ac.iscas.oncecloudshare.service.dao.authorization;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.account.Department;



public interface DepartmentDao extends PagingAndSortingRepository<Department, Long>,
	JpaSpecificationExecutor<Department>{
	
	public List<Department> findByParent(Department d);
	
	public List<Department> findByName(String name,Pageable pageable);

	public Department findByRoute(String route);
	
	@Query("FROM Department WHERE parent is null")
	public Department findRoot();
	
//	@Query("select d from Department as d where d.route like ?1")
	public List<Department> findByRouteLike(String route);
}