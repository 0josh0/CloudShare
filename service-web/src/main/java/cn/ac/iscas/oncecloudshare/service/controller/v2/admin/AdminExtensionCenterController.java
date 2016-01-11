//package cn.ac.iscas.oncecloudshare.service.controller.v2.admin;
//
//import java.io.IOException;
//
//import javax.annotation.Resource;
//
//import net.lingala.zip4j.exception.ZipException;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
//import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
//import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
//import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
//import cn.ac.iscas.oncecloudshare.service.model.common.ExtensionInfo;
//import cn.ac.iscas.oncecloudshare.service.service.common.ExtensionCenterService;
//import cn.ac.iscas.oncecloudshare.service.system.ServerInfo;
//import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
//import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
//
//@Controller
//@RequestMapping(value = "/adminapi/v2/extcenter", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
//public class AdminExtensionCenterController extends BaseController {
//	@Resource
//	private ExtensionCenterService extensionCenterService;
//
//	/**
//	 * 查找所有插件
//	 * 
//	 * @param q
//	 * @param pageParam
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.GET, params = "!supported")
//	@ResponseBody
//	public String findAll(@RequestParam(required = false) String q, PageParam pageParam) {
//		return Gsons.filterByFields(ExtensionInfo.class, pageParam.getFields()).toJson(extensionCenterService.findAll(q, pageParam));
//	}
//
//	/**
//	 * 查找可安装的插件
//	 * 
//	 * @param q
//	 * @param pageParam
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.GET, params = "supported")
//	@ResponseBody
//	public String findSupported(@RequestParam(required = false) String q, PageParam pageParam) {
//		String buildNumber = ServerInfo.getBuildNumber();
//		String append = "minSupport::lte::" + buildNumber + ",,maxSupport::gte::" + buildNumber;
//		if (StringUtils.isEmpty(q)) {
//			q = append;
//		} else {
//			q = q + ",," + append;
//		}
//		return Gsons.filterByFields(ExtensionInfo.class, pageParam.getFields()).toJson(extensionCenterService.findAll(q, pageParam));
//	}
//
//	/**
//	 * 下载并安装插件
//	 * 
//	 * @param id
//	 * @return
//	 */
//	@RequestMapping(value = "{id:\\d+}/install", method = RequestMethod.PUT)
//	@ResponseBody
//	public String downloadAndInstall(@PathVariable long id) {
//		ExtensionInfo extensionInfo = extensionCenterService.findOne(id);
//		if (extensionInfo == null) {
//			throw new RestException(ErrorCode.EXT_NOT_FOUND);
//		}
//		if (!extensionInfo.supported) {
//			throw new RestException(ErrorCode.UNSUPPORTED_EXT);
//		}
//		if (extensionInfo.enabled) {
//			throw new RestException(ErrorCode.INSTALLED_EXT);
//		}
//		// TODO:查询是否安装有更新的版本
//		try {
//			extensionCenterService.install(id);
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RestException(ErrorCode.BAD_EXT_FILE);
//		} catch (ZipException e) {
//			e.printStackTrace();
//			throw new RestException(ErrorCode.BAD_EXT_FILE);
//		}
//		return ok();
//	}
//}