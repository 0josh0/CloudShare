package cn.ac.iscas.oncecloudshare.service.extensions.device.utils;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;

public class DeviceUtils {
	public static class ErrorCodes {
		/**
		 * 设备需要审核
		 */
		public static final ErrorCode DEVICE_REVIEW_REQUIRED = new ErrorCode(403, 60, "device review required");
		/**
		 * 设备被禁用了
		 */
		public static final ErrorCode DEVICE_DISABLED = new ErrorCode(403, 61, "device disabled");
		/**
		 * 预料外的设备状态
		 */
		public static final ErrorCode DEVICE_USER_STATUS_UNEXPECTED = new ErrorCode(403, 62);
	}
}
