package cn.ac.iscas.oncecloudshare.service.extensions.backup.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

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
import cn.ac.iscas.oncecloudshare.service.extensions.backup.dto.BackupDto;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.model.Backup;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.service.BackupService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

@Controller
@RequestMapping(value = "/adminapi/v2/exts/backups", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AdminBackupController extends BaseController {
	@Resource
	private BackupService backupService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String create() {
		return gson().toJson(backupService.backupDB());
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String page(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		if (StringUtils.isEmpty(pageParam.getSort())) {
			pageParam.setSort("-createTime");
		}
		Page<Backup> page = backupService.findAll(filters, pageParam.getPageable(Backup.class));
		return Gsons.filterByFields(BackupDto.class, pageParam.getFields()).toJson(PageDto.of(page, BackupDto.adminTransformer));
	}
	
	@RequestMapping(value = "{id:\\d+}", params="recover", method = RequestMethod.PUT)
	@ResponseBody
	public String recover(@PathVariable long id) {
		Backup backup = backupService.findOne(id);
		if (backup == null){
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		backupService.recover(backup);
		return gson().toJson(ResponseDto.OK);
	}
}