package cn.ac.iscas.oncecloudshare.messaging.model.authc;

import com.google.common.base.Objects;

public class UserInfo {

	private Long id;
	private String name;
	private UserStatus status;

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id=id;
	}

	public UserStatus getStatus(){
		return status;
	}

	public void setStatus(UserStatus status){
		this.status=status;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name=name;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this).add("id",id).add("status",status)
				.toString();
	}

	public enum UserStatus {
		/**
		 * 正常
		 */
		ACTIVE,
		
		/**
		 * 已邀请，尚未激活
		 */
		UNACTIVATED,
		
		/**
		 * 冻结
		 */
		FROZEN,
	}
}
