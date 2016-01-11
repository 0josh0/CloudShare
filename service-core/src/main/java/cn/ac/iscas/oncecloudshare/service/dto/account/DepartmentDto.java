package cn.ac.iscas.oncecloudshare.service.dto.account;

import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import com.google.common.base.Function;


public class DepartmentDto {

	public static final Function<Department,DepartmentDto> TRANSFORMER=
			new Function<Department,DepartmentDto>(){

				@Override
				public DepartmentDto apply(Department input){
					DepartmentDto dto=new DepartmentDto();
					dto.id=input.getId();
					dto.name=input.getName();
					dto.route=input.getRoute();
					if(input.getParent()!=null){
						dto.parentId=input.getParent().getId();
					}
					return dto;
				}
			};
			
	public Long id;
	public String name;
	public String route;
	public Long parentId;
	
	public static DepartmentDto of(Department department){
		return TRANSFORMER.apply(department);
	}
}
