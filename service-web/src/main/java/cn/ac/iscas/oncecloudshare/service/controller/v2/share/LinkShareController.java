package cn.ac.iscas.oncecloudshare.service.controller.v2.share;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import cn.ac.iscas.oncecloudshare.service.dto.share.LinkShareDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.model.share.LinkShare;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.service.share.ShareService;
import cn.ac.iscas.oncecloudshare.service.utils.DateUtils;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/api/v2/links", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class LinkShareController extends BaseController {
	static Logger _logger = LoggerFactory.getLogger(LinkShareController.class);

	@Resource
	private FileService fileService;
	@Resource
	private ShareService shareService;
	@Resource
	private TenantService tenantService;
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;
	
	/**
	 * Owner视图
	 */
	private Function<LinkShare, LinkShareDto.OwnerView> ownerViewTransformer = new Function<LinkShare, LinkShareDto.OwnerView>() {
		@Override
		public LinkShareDto.OwnerView apply(LinkShare input) {
			LinkShareDto.OwnerView output = LinkShareDto.OwnerView.TRANSFORMER.apply(input);
			if (output != null) {
				output.url = configService.getConfig(Configs.Keys.CLIENT_WEB_URL, "") + "/links/" + tenantService.getCurrentTenant().getId() + "-"+ input.getKey();
			}
			return output;
		}
	};
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String list(PageParam pageParam) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, String.valueOf(currentUserId())));
		Page<LinkShare> page = shareService.searchLinkShare(filters, pageParam.getPageable(LinkShare.class));
		return Gsons.filterByFields(LinkShareDto.OwnerView.class, pageParam.getFields()).toJson(
				PageDto.of(page, ownerViewTransformer));
	}

	/**
	 * @function 对文件 fileId 创建外链（允许用户对同一文件生成多次外链）
	 * @URL POST http://{servername}/api/rest/share/externalShare
	 * @权限 请求用户为文件的拥有者，即可进行操作
	 * 
	 * @param request
	 * @param fileId
	 * @return 返回新创建的外链信息
	 * @throws ParseException
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String createLinkShare(LinkShareDto.Request dto) {
		File file = findFile(dto.getFileId());

		LinkShare es = new LinkShare();
		es.setCreateTime(new Date());
		if (dto.getExpireTime() == null) {
			es.setNeverExpire();
		} else {
			es.setExpireTime(new Date(dto.getExpireTime()));
		}
		es.setOwner(currentUser());
		// 使用系统时间戳作为文件的外部id
		es.setKey(DateUtils.uniqueKeyFromDate(new Date()));
		es.setPass(dto.getPass());
		es.setDescription(dto.getDescription());
		es.setFile(file);
		if (!dto.getShareHeadVersion() && file.getHeadVersion() != null) {
			es.setFileVersion(file.getHeadVersion().getVersion());
		}
		es = shareService.saveLinkShare(es);
		LinkShareDto.OwnerView result = LinkShareDto.OwnerView.of(es);
		result.url = configService.getConfig(Configs.Keys.CLIENT_WEB_URL, "") + "/links/" + tenantService.getCurrentTenant().getId() + "-"+ es.getKey();
		return gson().toJson(result);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q, PageParam pageParam) {
		List<SearchFilter> filters = SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("owner.id", Operator.EQ, String.valueOf(currentUserId())));
		Page<LinkShare> page = shareService.searchLinkShare(filters, pageParam.getPageable(LinkShare.class));
		return Gsons.filterByFields(LinkShareDto.OwnerView.class, pageParam.getFields()).toJson(
				PageDto.of(page, ownerViewTransformer));
	}

	/**
	 * @function 查看某个外链共享的具体信息
	 * @URL GET http://{servername}/api/rest/share/externalShare/{shareId}
	 * @权限 请求用户为系统用户 且为共享的创建者 即可进行操作
	 * 
	 * @param request
	 * @param shareId
	 * @return 请求的外链的具体信息（非DELETED状态）
	 * @throws ParseException
	 */
	@RequestMapping(value = "{key}", method = RequestMethod.GET)
	@ResponseBody
	public String retriveLinkShare(final HttpServletRequest request, @PathVariable("key") String key) {
		LinkShare sharedLink = shareService.getLinkShareByKey(key);
		checkIsNull(sharedLink);
		// 如果是owner
		if (sharedLink.getOwner().getId().equals(currentUserId())) {
			return gson().toJson(LinkShareDto.OwnerView.of(sharedLink));
		}
		// 如果是其他用户
		else {
			checkIsExpired(sharedLink);
			return gson().toJson(LinkShareDto.AnonView.of(sharedLink));
		}
	}

	/**
	 * @function 更新外链共享
	 * @URL PUT http://{servername}/api/rest/share/externalShare/{shareId} pickUp,expireTime,status 为可选参数，只需要传递需要更新的字段即可
	 *      expireTime 的参数格式为 yyyy-MM-dd
	 * @条件 请求用户为外链创建者 且 外链未删除
	 * 
	 * @param request
	 * @param shareId
	 * @param pickUp
	 * @param expireTime
	 * @return 更新后的外链共享的信息
	 * @throws ParseException
	 */
	@RequestMapping(value = "{key}", method = RequestMethod.PUT)
	@ResponseBody
	public String updateLinkShare(HttpServletRequest request, @PathVariable("key") String key, LinkShareDto.Request dto) {
		LinkShare es = shareService.getLinkShareByKey(key);
		checkIsNull(es);
		checkIsOwner(es);

		if (dto.getExpireTime() == null) {
			es.setNeverExpire();
		} else {
			es.setExpireTime(new Date(dto.getExpireTime()));
		}
		if (dto.getShareHeadVersion()){
			es.setFileVersion(null);
		} else {
			es.setFileVersion(es.getFile().getHeadVersion().getVersion());
		}
		es.setPass(dto.getPass());
		es.setDescription(dto.getDescription());
		es = shareService.saveLinkShare(es);
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * @function 删除外链共享
	 * @URL PUT http://{servername}/api/rest/share/externalShare/{shareId}
	 * @条件 请求用户为外链创建者
	 * 
	 * @param request
	 * @param shareId
	 * @return 成功信息
	 */
	@RequestMapping(value = "{key}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteLinkShare(final HttpServletRequest request, @PathVariable("key") String key) {
		LinkShare sharedLink = shareService.getLinkShareByKey(key);
		if (sharedLink != null){
			checkIsOwner(sharedLink);
			shareService.deleteLinkShare(sharedLink);
		}
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "{key}/checkPass", method = RequestMethod.POST)
	@ResponseBody
	public String checkAvailabilityOfExternalFile(final HttpServletRequest request, @PathVariable("key") String key,
			@RequestParam(value = "pass", required = false, defaultValue = "") String pass) {
		LinkShare sharedLink = shareService.getLinkShareByKey(key);
		checkIsNull(sharedLink);
		checkPass(sharedLink, pass);
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * @function 下载外链文件
	 * @URL GET http://{servername}/api/rest/share/externalFile/{externalFileId} pickUp 为提取码，可选参数。使用defaultValue 时为不需要提取码
	 * 
	 * @param request
	 * @param externalFileId
	 * @param pickUp
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "{key}/content", method = RequestMethod.GET)
	public void retrieveExternalFile(final HttpServletRequest request, @PathVariable("key") String key,
			@RequestParam(value = "pass", required = false, defaultValue = "") String pass, final HttpServletResponse response)
			throws IOException {
		LinkShare sharedLink = shareService.getLinkShareByKey(key);
		checkIsNull(sharedLink);
		checkPass(sharedLink, pass);
		shareService.updateLinkShareDownloads(key);
		downloadFileVersion(sharedLink.getSharedFileVersion(), request, response);
	}

	// TODO: to reconstrcut

	protected File findFile(Long fileId) {
		File file = fileId == null ? null : fileService.findFile(fileId);
		if (file == null) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		return file;
	}
	
	public void checkIsNull(LinkShare sharedLink){
		if (sharedLink == null) {
			throw new RestException(ErrorCode.LINKSHARE_NOT_FOUND);
		}
		if (sharedLink.getFile().getStatus().equals(FileStatus.DELETED)){
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
	}
	
	public void checkIsOwner(LinkShare sharedLink){
		if (!sharedLink.getOwner().getId().equals(currentUserId())) {
			throw new RestException(ErrorCode.FILE_NOT_MODIFIABLE);
		}
	}
	
	public void checkIsExpired(LinkShare sharedLink){
		if (sharedLink.isExpired()) {
			throw new RestException(ErrorCode.LINKSHARE_GONE);
		}
	}
	
	public void checkPass(LinkShare sharedLink, String pass){
		if (sharedLink.isNeedPass() && !pass.equals(sharedLink.getPass())) {
			throw new RestException(ErrorCode.LINKSHARE_INVALID_PASS);
		}
	}
}
