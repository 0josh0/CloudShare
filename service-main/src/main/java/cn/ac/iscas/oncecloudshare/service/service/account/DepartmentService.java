package cn.ac.iscas.oncecloudshare.service.service.account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.DepartmentDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.exceptions.account.DuplicateDepartmentRouteException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidPathException;
import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Service
@Transactional
public class DepartmentService {

	private static Logger logger=LoggerFactory.getLogger(DepartmentService.class);

	@Autowired
	private DepartmentDao dDao;
	
	@Autowired
	private UserService userService;
	
	protected String concatPath(String basePath,String filename){
		String path=FilePathUtil.concatPath(basePath,filename);
		if(path==null){
			throw new InvalidPathException(basePath+"/"+filename);
		}
		return path;
	}
	
	protected void checkRouteNotExsists(String route){
		if(findByRoute(route)!=null){
			throw new DuplicateDepartmentRouteException(route);
		}
	}
	
	public Page<Department> search(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<Department> spec = Specifications.fromFilters(filters, Department.class);
			return dDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public Department find(Long id){
		return dDao.findOne(id);
	}
	
	public Department findExsitingOne(long id){
		Department department=find(id);
		if(department==null){
			throw new IllegalArgumentException("department not exists"+id);
		}
		return department;
	}

	public Page<Department> findAll(Pageable pageable){
		return dDao.findAll(pageable);
	}

	public Department findRoot(){
		return dDao.findRoot();
	}

//	public Department save(Department d){
//		return dDao.save(d);
//	}
//
//	public List<Department> findByParent(Department parent){
//		return dDao.findByParent(parent);
//	}

	public Department findByRoute(String route){
		return dDao.findByRoute(route);
	}

//	public List<Department> findAllAncestors(long deptId){
//		List<Department> list=new ArrayList<Department>();
//		Department d=dDao.findOne(deptId);
//		while(d!=null){
//			list.add(d);
//			d=d.getParent();
//		}
//		return list;
//	}

	public List<Department> findByRoutePrefix(String routePrefix){
		return dDao.findByRouteLike(routePrefix+"%");
	}
	
	/**
	 * 新增部门
	 * @param parentId
	 * @param name
	 */
	public Department add(long parentId,String name){
		Department parent=findExsitingOne(parentId);
		String route=concatPath(parent.getRoute(),name);
		checkRouteNotExsists(route);
		
		Department d=new Department();
		d.setParent(parent);
		d.setRoute(route);
		dDao.save(d);
		return d;
	}
	
	public void move(long deptId,long parentId,String name){
		Department dept=findExsitingOne(deptId);
		Department parent=findExsitingOne(parentId);
		
		final int oldRouteLen=dept.getRoute().length();
		final String newRoute=concatPath(parent.getRoute(),name);
		
		checkRouteNotExsists(newRoute);
		if(newRoute.startsWith(dept.getRoute() + "/")){
			throw new IllegalArgumentException("cannot move to a child department");
		}
		
		dept.setParent(parent);
		dept.setRoute(newRoute);
		dDao.save(dept);
		
		executeRecursively(dept,new RecursiveAction(){
			
			@Override
			public void apply(Department d){
				d.setRoute(newRoute+d.getRoute().substring(oldRouteLen));
				dDao.save(d);
			}
		});
	
	}
	
	/**
	 * 删除部门
	 * @param department
	 */
	public void delete(long id){
		Department department=findExsitingOne(id);
		Preconditions.checkArgument(department.getParent()!=null,
				"cannot delete root department");
		final List<Department> toDelete=Lists.newArrayList();
		final List<Long> toDeleteIds=Lists.newArrayList();
		toDelete.add(department);
		toDeleteIds.add(department.getId());
		executeRecursively(department,new RecursiveAction(){
			
			@Override
			public void apply(Department d){
				toDelete.add(d);
				toDeleteIds.add(d.getId());
			}
		});
		userService.detachDeparment(toDeleteIds);
		dDao.delete(toDelete);
	}

	private void executeRecursively(Department d,RecursiveAction action){
		List<Department> children=ImmutableList.copyOf(d.getChildren());
		for(Department child:children){
			executeRecursively(child,action);
			action.apply(child);
		}
	}
	
	private static interface RecursiveAction{
		void apply(Department d);
	}
}
