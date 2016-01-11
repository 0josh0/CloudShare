package cn.ac.iscas.oncecloudshare.service.dto.account;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.model.account.RoleEntry;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserProfile;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class UserDto {

	public static final Function<User, UserDto> ANON_TRANSFORMER = new Function<User, UserDto>() {

		@Override
		public UserDto apply(User input) {
			UserDto dto = new UserDto();
			dto.id = input.getId();
			dto.name = input.getName();
			dto.email = input.getEmail();
			if (input.getDepartment() != null) {
				dto.departmentId = input.getDepartment().getId();
				dto.departmentName = input.getDepartment().getName();
			}
			dto.profile = input.getProfile();
			dto.status = input.getStatus();
			if (input.getCreateTime() != null) {
				dto.createTime = input.getCreateTime().getTime();
			}

			dto.signature = input.getSignature();

			dto.phoneNumber = input.getProfile().getTel();
			return dto;
		}
	};

	public static final Function<User, UserDto> OWNER_TRANSFORMER = new Function<User, UserDto>() {

		@Override
		public UserDto apply(User input) {
			UserDto dto = ANON_TRANSFORMER.apply(input);
			dto.quota = input.getQuota();
			dto.restQuota = input.getRestQuota();
			if (Collections3.isNotEmpty(input.getRoleEntries())) {
				dto.roles = Lists.newArrayList();
				for (RoleEntry entry : input.getRoleEntries()) {
					dto.roles.add(entry.toShiroRoleIdentifier());
				}
			}
			return dto;
		}

	};

	public static final Function<User, UserDto> ADMIN_TRANSFORMER = new Function<User, UserDto>() {
		@Override
		public UserDto apply(User input) {
			UserDto dto = OWNER_TRANSFORMER.apply(input);
			dto.profile = null;
			return dto;
		}

	};

	/**
	 * 最简要的转换器，比如文件所有者，上传者，创建者之类的
	 */
	public static final Function<User, UserDto> GLANCE_TRANSFORMER = new Function<User, UserDto>() {
		@Override
		public UserDto apply(User input) {
			if (input == null) {
				return null;
			}
			UserDto dto = new UserDto();
			dto.id = input.getId();
			dto.name = input.getName();
			dto.email = input.getEmail();
			if (input.getDepartment() != null) {
				dto.departmentName = input.getDepartment().getName();
			}
			return dto;
		}
	};

	public Long id;
	public String name;
	public String email;
	public Long departmentId;
	public String departmentName;
	public UserProfile profile;

	public Long quota;
	public Long restQuota;
	public List<String> roles;

	public UserStatus status;
	public Long createTime;

	public String signature;

	public String phoneNumber;

	public static UserDto forAnon(User user) {
		return ANON_TRANSFORMER.apply(user);
	}

	public static UserDto forOwner(User user) {
		return OWNER_TRANSFORMER.apply(user);
	}

	public static UserDto forAdmin(User user) {
		return ADMIN_TRANSFORMER.apply(user);
	}

	public static class Request {

		public String name;
		public String plainPasword;
		public String email;
		public Long departmentId;
		public Long quota;
	}

	public static UserDto forMessaging(User user) {
		UserDto dto = new UserDto();
		dto.name = user.getName();
		dto.id = user.getId();
		dto.status = user.getStatus();
		return dto;
	}

	public static class BatchAddRequest {

		public String name;
		public String plainPasword;
		public String email;
		public Long departmentId;
		public Long quota;
	}

	public static class BatchInviteRequest {

		public String name;
		public String email;
		public Long departmentId;
		public Long quota;
	}

	public static class BatchReviewRequest {
		// 要审核的用户的id
		@NotNull
		@Size(min = 1, max = 50)
		private long[] ids;
		// 是否通过
		private boolean agreed;

		public long[] getIds() {
			return ids;
		}

		public void setIds(long[] ids) {
			this.ids = ids;
		}

		public boolean isAgreed() {
			return agreed;
		}

		public void setAgreed(boolean agreed) {
			this.agreed = agreed;
		}
	}

	public static class ReviewResponse {
		public long userId;
		public boolean success;
		// 处理出错的原因
		public ErrorCode errorCode;
	}
}
