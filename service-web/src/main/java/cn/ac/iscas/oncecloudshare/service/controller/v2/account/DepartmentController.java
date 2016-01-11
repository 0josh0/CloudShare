package cn.ac.iscas.oncecloudshare.service.controller.v2.account;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.annotation.AnonApi;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.DepartmentDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.service.account.DepartmentService;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

@Controller
@RequestMapping (value="/api/v2/departments",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class DepartmentController extends BaseController{
	@Resource
	private DepartmentService dService;
	@Resource
	private UserService userService;
	
	Department findDepartment(String id){
		if(id.equals("root")){
			return dService.findRoot();
		}
		Long deptId=Longs.tryParse(id);
		if(deptId==null){
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		Department department=dService.find(deptId);
		if(department==null){
			throw new RestException(ErrorCode.DEPARTMENT_NOT_FOUND);
		}
		return department;
	}
	
	@RequestMapping(value="",method=RequestMethod.GET)
	@ResponseBody
	public String list(PageParam pageParam){
		Page<Department> page=dService.findAll(pageParam.getPageable(Department.class));
		return Gsons.filterByFields(DepartmentDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,DepartmentDto.TRANSFORMER));
	}
	
	@RequestMapping(value="{id:root|\\d+}",method=RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String id){
		return gson().toJson(DepartmentDto.of(findDepartment(id)));
	}
	
	@AnonApi
	@RequestMapping(value="search",method=RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q,PageParam pageParam){
		List<SearchFilter> filters=SearchFilter.parseQuery(q);
		if (!isAuthenticatedUser()) {
			// 如果没有登录，只能按path查询
			filters = Lists.newArrayList(Iterables.filter(filters, new Predicate<SearchFilter>() {
				@Override
				public boolean apply(SearchFilter input) {
					return input.fieldName.equals("route") && input.operator == Operator.EQ;
				}
			}));
			if (filters.isEmpty()) {
				filters.add(new SearchFilter("id", Operator.EQ, -1));
			}
			pageParam.setFields(Lists.newArrayList("id", "name", "route"));
		}
		
		Page<Department> page=dService.search(filters,pageParam.getPageable(Department.class));
		return Gsons.filterByFields(DepartmentDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,DepartmentDto.TRANSFORMER));
	}
	
	@RequestMapping(value = "{id:root|\\d+}/members", method = RequestMethod.GET)
	@ResponseBody
	public String members(@PathVariable String id) {
		Department department = findDepartment(id);
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("department", Operator.EQ, department));
		List<User> users = userService.findAll(filters);
		return gson().toJson(Lists.transform(users, UserDto.ANON_TRANSFORMER));
	}
}
