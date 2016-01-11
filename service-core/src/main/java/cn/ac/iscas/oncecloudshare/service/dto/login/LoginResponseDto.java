package cn.ac.iscas.oncecloudshare.service.dto.login;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.model.account.RoleEntry;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;

import com.google.common.collect.Lists;

/**
 * 登录返回信息
 * 
 * @author One
 * 
 */
public class LoginResponseDto {

	public final String ticket;
	public final Long tenantId;
	public final Long userId;
	public final String userName;
	public final Long quota;
	public final Long restQuota;
	public final UserStatus status;
	public final String email;
	public final List<String> roles;

	public final String signature;

	public LoginResponseDto(String ticket, Long tenantId, User user){

		this.ticket=ticket;
		this.tenantId=tenantId;
		this.userId=user.getId();
		this.userName=user.getName();
		this.quota=user.getQuota();
		this.restQuota=user.getRestQuota();
		this.email=user.getEmail();
		this.status=user.getStatus();

		roles=Lists.newArrayList();
		for(RoleEntry entry: user.getRoleEntries()){
			roles.add(entry.toShiroRoleIdentifier());
		}

		this.signature=user.getSignature();
	}
}