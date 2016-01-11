package cn.ac.iscas.oncecloudshare.service.controller.v2.common;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
import cn.ac.iscas.oncecloudshare.service.dto.common.ConfigDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.common.GlobalConfig;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

@Controller
@RequestMapping (value="/api/v2/configs",
	produces={MediaTypes.TEXT_PLAIN_UTF8,MediaTypes.JSON_UTF8})
public class ConfigController extends BaseController{

	private GlobalConfig findConfig(String key){
		GlobalConfig c=globalConfigService.find(key);
		if(c==null || !c.getNormalUserReadable()){
			throw new RestException(ErrorCode.CONFIG_NOT_FOUND);
		}
		return c;
	}
	
	@RequestMapping(value="{key:[a-zA-Z0-9\\._]+}",method=RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String key){
		GlobalConfig config=findConfig(key);
		return gson().toJson(ConfigDto.forAnon(config));
	}
	
	@RequestMapping(value="search",method=RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q,PageParam pageParam){
		List<SearchFilter> filters=SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("normalUserReadable",Operator.EQ,true));
		Page<GlobalConfig> page=globalConfigService.search(filters,pageParam.getPageable(GlobalConfig.class));
		return Gsons.filterByFields(ConfigDto.class,pageParam.getFields())
				.toJson(PageDto.of(page,ConfigDto.ANON_TRANSFORMER));
	}
	
	@RequestMapping(value="grouped/company",method=RequestMethod.GET)
	@ResponseBody
	public String getCompanyInfo() {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("normalUserReadable", Operator.EQ, true));
		filters.add(new SearchFilter("key", Operator.LIKE, "sys.company.%"));
		List<GlobalConfig> configs = globalConfigService.search(filters);
		return gson().toJson(Lists.transform(configs, ConfigDto.ANON_TRANSFORMER));
	}
	
	@RequestMapping(value = "grouped/company/logo", method = RequestMethod.GET)
	public ResponseEntity<?> downloadCompanyLogo(HttpServletRequest request, HttpServletResponse response) throws IOException {
		GlobalConfig config = findConfig("sys.company.logo");
		if (config == null || Strings.isNullOrEmpty(config.getValue())){
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		String md5 = config.getValue();
		setETag(response, md5);
		if (matchETagStrong(request, md5)) {
			return NOT_MODIFIED;
		}
		ByteSource source = runtimeContext.getFileStorageService().retrieveFileContent(md5);
		if (source != null) {
			initDownload(request, response, source, null);
		}
		return OK;
	}
}
