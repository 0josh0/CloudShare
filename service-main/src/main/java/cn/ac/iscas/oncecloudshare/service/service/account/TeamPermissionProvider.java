package cn.ac.iscas.oncecloudshare.service.service.account;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;

public interface TeamPermissionProvider {
	/**
	 * 得到Team的所有角色
	 * 
	 * @return
	 */
	public List<String> getRoles();
	
	/**
	 * 得到角色对Team的所有权限 
	 *
	 * @param role
	 * @return
	 */
	public List<String> getPermissions(String role);
	
	/**
	 * 得到角色对应成员的所有权限
	 *
	 * @param role
	 * @param teamMate
	 * @return
	 */
	public List<String> getPermissions(String role, TeamMate teamMate);
	
	/**
	 * 得到角色对角色2的所有权限 
	 *
	 * @param role
	 * @param role2
	 * @return
	 */
	public List<String> getPermissions(String role, String role2);
}
