package cn.ac.iscas.oncecloudshare.service.controller.v2.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.DepartmentDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import cn.ac.iscas.oncecloudshare.service.service.account.DepartmentService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.primitives.Longs;

@Controller
@RequestMapping (value="/adminapi/v2/departments",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class AdminDepartmentController extends BaseController {

	@Autowired
	DepartmentService dService;
	
	Department findDepartment(String id){
		if(id.equals("root")){
			return dService.findRoot();
		}
		return findDepartment(Longs.tryParse(id));
	}
	
	Department findDepartment(Long id){
		if(id==null){
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		Department department=dService.find(id);
		if(department==null){
			throw new RestException(ErrorCode.DEPARTMENT_NOT_FOUND);
		}
		return department;
	}
	
	Department findParent(Long id){
		Department department=dService.find(id);
		if(department==null){
			throw new RestException(ErrorCode.PARENT_DEPARTMENT_NOT_EXSISTS);
		}
		return department;
	}
	
	@RequestMapping(value="",method=RequestMethod.POST)
	@ResponseBody
	public String add(@RequestParam Long parentId,
			@RequestParam String name){
		Department parent=findParent(parentId);
		Department d=dService.add(parent.getId(),name);
		return gson().toJson(DepartmentDto.of(d));
	}
	
	@RequestMapping(value="{id:\\d+}",method=RequestMethod.PUT)
	@ResponseBody
	public String move(@PathVariable Long id,
			@RequestParam Long parentId,
			@RequestParam String name){
		Department parent=findParent(parentId);
		Department d=findDepartment(id);
		dService.move(id,parentId,name);
		return gson().toJson(ResponseDto.OK);
	}
	
	@RequestMapping(value="{id:\\d+}",method=RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long id){
		Department d=findDepartment(id);
		dService.delete(d.getId());
		return gson().toJson(ResponseDto.OK);
	}
}
