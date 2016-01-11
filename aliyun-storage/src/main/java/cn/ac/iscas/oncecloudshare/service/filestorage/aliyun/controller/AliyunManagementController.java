package cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.dto.AliyunConfigDto;
import cn.ac.iscas.oncecloudshare.service.filestorage.aliyun.service.AliyunStorageService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;

@Controller
@RequestMapping(value = { "/adminapi/v2/storages/aliyun" })
public class AliyunManagementController extends BaseController {
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	@Resource
	private AliyunStorageService aliyunStorageService;
	
	@RequestMapping(value = "configs", method = RequestMethod.PUT)
	@ResponseBody
	public String config(@Valid AliyunConfigDto dto) {
		configService.saveConfig(Configs.Keys.ALIYUN_KEY_ID, dto.getKey(), true);
		configService.saveConfig(Configs.Keys.ALIYUN_KEY_SECRET, dto.getSecret(), true);
		aliyunStorageService.refreshOSSClient();
		return gson().toJson(ResponseDto.OK);
	}
}
