package cn.ac.iscas.oncecloudshare.service.controller.v2.admin;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.fusesource.hawtbuf.ByteArrayInputStream;
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
import cn.ac.iscas.oncecloudshare.service.dto.common.ConfigDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.AccessMode;
import cn.ac.iscas.oncecloudshare.service.model.common.GlobalConfig;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService.ConfigUtil;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;

@Controller
@RequestMapping(value = "/adminapi/v2/configs", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AdminConfigController extends BaseController {
	@Resource
	private MailService mailService;

	private GlobalConfig findConfig(String key) {
		GlobalConfig c = globalConfigService.find(key);
		if (c == null || !c.adminReadable()) {
			throw new RestException(ErrorCode.CONFIG_NOT_FOUND);
		}
		return c;
	}

	@RequestMapping(value = "{key:[a-zA-Z0-9\\._]+}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String key) {
		GlobalConfig config = findConfig(key);
		return gson().toJson(ConfigDto.forAdmin(config));
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q, PageParam pageParam) {
		List<SearchFilter> filters = SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("adminAccessMode", Operator.NE, AccessMode.NONE));
		Page<GlobalConfig> page = globalConfigService.search(filters, pageParam.getPageable(GlobalConfig.class));
		return Gsons.filterByFields(ConfigDto.class, pageParam.getFields()).toJson(PageDto.of(page, ConfigDto.ADMIN_TRANSFORMER));
	}

	@RequestMapping(value = "{key:[a-zA-Z0-9\\._]+}", method = RequestMethod.PUT)
	@ResponseBody
	public String update(@PathVariable String key, @RequestParam String value) {
		GlobalConfig config = findConfig(key);
		if (config.adminWritable()) {
			Object val = ConfigUtil.parseConfigValue(value, config.getDataType());
			Preconditions.checkArgument(val != null, "illegal value");
			globalConfigService.saveConfig(key, val.toString(), false);
		}
		return ok();
	}

	@RequestMapping(value = "{key:[a-zA-Z0-9\\._]+}", method = RequestMethod.PUT, params = "displayGroup")
	@ResponseBody
	public String updateDisplayGroup(@PathVariable String key, @RequestParam String displayGroup) {
		GlobalConfig config = findConfig(key);
		config.setDisplayGroup(displayGroup);
		globalConfigService.saveConfig(config);
		return ok();
	}

	@RequestMapping(value = { "sendTestMail" }, method = RequestMethod.POST)
	@ResponseBody
	public String sendTestMail() {
		@SuppressWarnings("unchecked")
		Mail mail = new Mail("test mail", "test mail", Collections.EMPTY_LIST);
		Future<Boolean> future = mailService.send(globalConfigService.getConfig(Configs.Keys.MAIL_ACCOUNT, ""), mail);
		Map<String, Object> results = Maps.newHashMap();
		try {
			if (future.get()) {
				results.put("success", Boolean.TRUE);
			} else {
				results.put("success", Boolean.FALSE);
			}
		} catch (Exception e) {
			results.put("success", Boolean.FALSE);
			results.put("errorMsg", e.getMessage());
		}
		return gson().toJson(results);
	}

	@RequestMapping(value = "company/logo", method = RequestMethod.POST, params = "base64")
	@ResponseBody
	public String uploadCompanyLogo(@RequestParam final String base64) throws IOException {
		final BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(base64)));
		ByteSource source = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(img, "png", out);
				return new ByteArrayInputStream(out.toByteArray());
			}
		};
		String md5 = runtimeContext.getFileStorageService().saveFile(source).getMd5();
		update("sys.company.logo", md5);
		return gson().toJson(ResponseDto.OK);
	}
}