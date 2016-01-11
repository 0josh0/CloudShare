package cn.ac.iscas.oncecloudshare.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.dto.StorageDeviceDto;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.exceptions.DeviceNotFoundException;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.model.StorageDevice;
import cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.StorageDeviceService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

@Controller
@RequestMapping(value = { "/adminapi/v2/storagedevices" })
public class StorageManagementController extends BaseController {
	@Autowired
	private StorageDeviceService deviceService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String addDevice(@RequestParam(value = "deviceUri") String uri) {
		StorageDevice device = deviceService.addDevice(uri);
		return gson().toJson(StorageDeviceDto.of(device));
	}

	@RequestMapping(value = "/{deviceId}", method = RequestMethod.PUT)
	@ResponseBody
	public String updateDevice(@PathVariable(value = "deviceId") Long deviceId,
			@RequestParam(value = "deviceUri", required = false) String uri,
			@RequestParam(value = "status", required = false) String status) {
		StorageDevice device = deviceService.updateDevice(deviceId, uri,
				StorageDevice.DeviceStatus.of(status));
		return gson().toJson(StorageDeviceDto.of(device));
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public String retrieveDevices(@RequestParam String q, PageParam pageParam) {
		Page<StorageDevice> page = deviceService.search(q,
				pageParam.getPageable(StorageDevice.class));
		return Gsons.filterByFields(StorageDeviceDto.class,
				pageParam.getFields()).toJson(
				PageDto.of(page, StorageDeviceDto.TRANSFORMER));
	}

	@RequestMapping(value = "/{deviceId}", method = RequestMethod.GET)
	@ResponseBody
	public String retrieveDevice(@PathVariable("deviceId") Long deviceId) {
		StorageDevice device = deviceService.findDeviceById(deviceId);
		if (device == null) {
			throw new DeviceNotFoundException("the requested device with id "
					+ deviceId + " not exists");
		}
		
		return gson().toJson(StorageDeviceDto.of(device));
	}

}
