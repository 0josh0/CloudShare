package cn.ac.iscas.oncecloudshare.service.service.multitenancy;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;


public interface TenantService {

	/**
	 * 得到当前租户
	 * 
	 * @return
	 */
	@Nullable Tenant getCurrentTenant();

	/**
	 * 设置当前租户
	 * 
	 * @param id
	 * @return false 如果对应的tenant不存在
	 */
	boolean setCurrentTenant(@Nullable Long id);
	
	boolean setCurrentTenantManually(long id);
	
	/**
	 * 清除当前线程的租户信息
	 */
	void clearCurrentTenant();
	
	/**
	 * 查找租户
	 * @param id
	 * @return
	 */
	@Nullable Tenant find(@Nullable Long id);
	
	/**
	 * 查找租户 
	 *
	 * @return
	 */
	@Nonnull List<Tenant> findAll(@Nullable List<SearchFilter> filters);
}
