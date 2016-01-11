package cn.ac.iscas.oncecloudshare.messaging.controller.iospush;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.messaging.controller.BaseController;
import cn.ac.iscas.oncecloudshare.messaging.controller.PageParam;
import cn.ac.iscas.oncecloudshare.messaging.dto.PageDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.iospush.IOSDeviceDto;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.messaging.model.iospush.IOSDevice;
import cn.ac.iscas.oncecloudshare.messaging.service.iospush.IOSDeviceService;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;

@Controller
@RequestMapping (value="/api/iosdevices",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class IOSDeviceController extends BaseController{
	
	@Autowired
	IOSDeviceService dService;
	
	private IOSDevice findDevice(Long id,Long userId){
		IOSDevice device=dService.find(id);
		if(device!=null && device.getUserId().equals(userId)){
			return device;
		}
		throw new RestException(HttpStatus.NOT_FOUND,"device not exists"); 
	}

	@RequestMapping(value="",method=RequestMethod.GET)
	@ResponseBody
	public String listAll(PageParam pageParam){
		Long userId=currentUserId();
		Page<IOSDevice> page=dService.findByUserId(userId,
				pageParam.getPageable(IOSDevice.class));
		return Gsons.filterByFields(IOSDeviceDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,IOSDeviceDto.TRANSFORMER));
	}
	
	@RequestMapping(value="",method=RequestMethod.POST)
	@ResponseBody
	public String addDevice(@RequestParam String deviceToken,
			@RequestParam String description){
		Long userId=currentUserId();
		dService.addDevice(userId,deviceToken,description);
		return ok();
	}
	
	@RequestMapping(value="/{token}",method=RequestMethod.DELETE)
	@ResponseBody
	public String deleteDevice(@PathVariable String token){
		Long userId=currentUserId();
		dService.deleteByUserIdAndDeviceToken(userId,token);
		return ok();
	}
}
