package cn.ac.iscas.oncecloudshare.service.extensions.device.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dto.DeviceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dto.DeviceLoginDto;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dto.DeviceUserDto;
import cn.ac.iscas.oncecloudshare.service.extensions.device.events.AdminDeviceEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.Device;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceLogin;
import cn.ac.iscas.oncecloudshare.service.extensions.device.model.DeviceUser;
import cn.ac.iscas.oncecloudshare.service.extensions.device.service.DeviceService;
import cn.ac.iscas.oncecloudshare.service.extensions.device.utils.DeviceUtils;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

import com.google.common.collect.Lists;

/**
 * adminapi:extension.deivce相关功能，包括：
 * 
 * <pre>
 * 获取登录日志    GET		/adminapi/v2/extensions/devices/logins
 * 查询登录日志    GET		/adminapi/v2/extensions/devices/logins/search
 * 获取设备用户    GET		/adminapi/v2/extensions/devices/users
 * 查询设备用户    GET		/adminapi/v2/extensions/devices/users/search
 * 批量审核设备	PUT		/adminapi/v2/extensions/devices/users?review
 * </pre>
 * 
 * @version
 * @since JDK 1.6
 */
@Controller
@RequestMapping(value = "/adminapi/v2/exts/devices", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AdminDeviceController extends BaseController {

	@Resource
	private DeviceService deviceService;

	@RequestMapping(value = { "logins", "logins/search" }, method = RequestMethod.GET)
	@ResponseBody
	public String listDeviceLogins(@RequestParam(required = false) String q, @RequestParam(required = false) String o, PageParam pageParam) {
		List<SearchFilter> and = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		List<SearchFilter> or = StringUtils.isEmpty(o) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(o);
		Page<DeviceLogin> page = deviceService.findLogins(and, or, pageParam.getPageable(DeviceLogin.class));
		return Gsons.filterByFields(DeviceLoginDto.class, pageParam.getFields()).toJson(PageDto.of(page, DeviceLoginDto.AdminTransformer));
	}

	@RequestMapping(value = { "/users", "users/search" }, method = RequestMethod.GET)
	@ResponseBody
	public String listDeviceUsers(@RequestParam(required = false) String q, @RequestParam(required = false) String o, PageParam pageParam) {
		List<SearchFilter> and = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		List<SearchFilter> or = StringUtils.isEmpty(o) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(o);
		Page<DeviceUser> page = deviceService.findDeviceUsers(and, or, pageParam.getPageable(DeviceUser.class));
		return Gsons.filterByFields(DeviceUserDto.class, pageParam.getFields()).toJson(PageDto.of(page, DeviceUserDto.DefaultTransformer));
	}

	@RequestMapping(value = "/users/{id:\\d+}", params = "enable", method = RequestMethod.PUT)
	@ResponseBody
	public String updateDeviceUser(@PathVariable long id, @RequestParam boolean enable) {
		DeviceUser deviceUser = deviceService.findDeviceUser(id);
		if (deviceUser == null) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		int eventType;
		if (deviceUser.getStatus().equals(DeviceUser.Status.APPLYING)) {
			deviceUser.setReviewBy(currentUser());
			deviceUser.setReviewTime(new Date());
			eventType = enable ? AdminDeviceEvent.EVENT_AGREED : AdminDeviceEvent.EVENT_DISAGREED;
		} else {
			eventType = enable ? AdminDeviceEvent.EVENT_ENABLED : AdminDeviceEvent.EVENT_DISABLED;
		}
		deviceUser.setStatus(enable ? DeviceUser.Status.ENABLE : DeviceUser.Status.DISABLED);
		deviceService.saveDeviceUser(deviceUser);
		postEvent(new AdminDeviceEvent(getUserPrincipal(), deviceUser, eventType));
		return gson().toJson(ResponseDto.OK);
	}
	
	@RequestMapping(value = "/users", params = "review", method = RequestMethod.PUT)
	@ResponseBody
	public String batchReview(@Valid DeviceUserDto.BatchReviewRequest request) {
		List<DeviceUserDto.ReviewResponse> responses = Lists.newArrayList();
		for (long id : request.getIds()) {
			DeviceUserDto.ReviewResponse response = new DeviceUserDto.ReviewResponse();
			responses.add(response);
			response.id = id;

			DeviceUser deviceUser = deviceService.findDeviceUser(id);
			if (deviceUser == null) {
				response.success = false;
				response.errorCode = ErrorCode.NOT_FOUND;
			} else if (!DeviceUser.Status.APPLYING.equals(deviceUser.getStatus())) {
				response.success = false;
				response.errorCode = new ErrorCode(DeviceUtils.ErrorCodes.DEVICE_USER_STATUS_UNEXPECTED.statusCode,
						DeviceUtils.ErrorCodes.DEVICE_USER_STATUS_UNEXPECTED.subCode, deviceUser.getStatus().name());
			} else {
				response.success = true;
				deviceUser.setStatus(request.isAgreed() ? DeviceUser.Status.ENABLE : DeviceUser.Status.DISABLED);
				deviceUser.setReviewBy(currentUser());
				deviceUser.setReviewTime(new Date());
				deviceService.saveDeviceUser(deviceUser);
			}
		}
		return gson().toJson(responses);
	}

	@RequestMapping(value = { "", "search" }, method = RequestMethod.GET)
	@ResponseBody
	public String listDevices(@RequestParam(required = false) String q, @RequestParam(required = false) String o, PageParam pageParam) {
		List<SearchFilter> and = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		List<SearchFilter> or = StringUtils.isEmpty(o) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(o);
		Page<Device> page = deviceService.findDevices(and, or, pageParam.getPageable(Device.class));
		return Gsons.filterByFields(DeviceDto.class, pageParam.getFields()).toJson(PageDto.of(page, DeviceDto.DefaultTransformer));
	}
}
