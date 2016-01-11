package cn.ac.iscas.oncecloudshare.service.controller.v2.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.common.ActionLogDto;
import cn.ac.iscas.oncecloudshare.service.model.common.ActionLog;
import cn.ac.iscas.oncecloudshare.service.service.common.ActionLogService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

@Controller
@RequestMapping(value = "/api/v2/logs", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ActionLogController extends BaseController {
	@Resource
	private ActionLogService actionLogService;

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("user.id", Operator.EQ, currentUserId()));
		Page<ActionLog> page = actionLogService.findAll(filters, pageParam.getPageable(ActionLog.class));
		return Gsons.filterByFields(ActionLogDto.class, pageParam.getFields()).toJson(PageDto.of(page, ActionLogDto.forUser));
	}
	
	@RequestMapping(value = "/types", method = RequestMethod.GET)
	@ResponseBody
	public String types() {
		return gson().toJson(actionLogService.findAllTargetTypes());
	}
}