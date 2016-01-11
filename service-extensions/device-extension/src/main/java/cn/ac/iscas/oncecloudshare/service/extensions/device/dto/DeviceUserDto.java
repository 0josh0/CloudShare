package cn.ac.iscas.oncecloudshare.service.extensions.device.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceUser;

import com.google.common.base.Function;

public class DeviceUserDto {
	public Long id;
	public UserDto user;
	public DeviceDto device;
	public Long applicationTime;
	public String status;
	// 审核人
	public UserDto reviewer;
	// 审核时间
	public Long reviewTime;
	// 登录次数
	public Long loginTimes;

	public static final Function<DeviceUser, DeviceUserDto> DefaultTransformer = new Function<DeviceUser, DeviceUserDto>() {
		public DeviceUserDto apply(DeviceUser input) {
			if (input == null) {
				return null;
			}
			DeviceUserDto output = new DeviceUserDto();
			output.id = input.getId();
			output.user = UserDto.DefaultTranformer.apply(input.getUser());
			output.device = DeviceDto.DefaultTransformer.apply(input.getDevice());
			if (input.getCreateTime() != null) {
				output.applicationTime = input.getCreateTime().getTime();
			}
			if (input.getStatus() != null) {
				output.status = input.getStatus().toString();
			}
			output.reviewer = UserDto.DefaultTranformer.apply(input.getReviewBy());
			if (input.getReviewTime() != null) {
				output.reviewTime = input.getReviewTime().getTime();
			}
			output.loginTimes = input.getLoginTimes();
			return output;
		}
	};

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
		public long id;
		public boolean success;
		// 处理出错的原因
		public ErrorCode errorCode;
	}
}