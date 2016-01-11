package cn.ac.iscas.oncecloudshare.service.controller.v2.share;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.NotifType;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.share.ReceivedShareDto;
import cn.ac.iscas.oncecloudshare.service.dto.share.ShareDto;
import cn.ac.iscas.oncecloudshare.service.dto.share.ShareDto.CreateRequest;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.model.share.ShareRecipient;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.share.ReceivedShareService;
import cn.ac.iscas.oncecloudshare.service.service.share.ShareService2;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * 提供分享中心的相关功能，包括：
 * 
 * <pre>
 * 获取我的分享    GET		/api/v2/shares
 * 搜索我的分享    GET		/api/v2/shares/search
 * 创建分享			POST		/api/v2/shares
 * 
 * 获取我收到的分享    GET		/api/v2/shares/received
 * 搜索我收到的分享    GET		/api/v2/shares/received/search
 * </pre>
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Controller
@RequestMapping(value = "/api/v2/shares", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class SharesController extends BaseController {
	private static final Logger _logger = LoggerFactory.getLogger(SharesController.class);
	@Resource
	private ShareService2 shareService2;
	@Resource
	private FileService fileService;
	@Resource
	private ReceivedShareService receivedShareService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String page(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("creator.id", Operator.EQ, currentUserId()));
		filters.add(new SearchFilter("status", Operator.EQ, Share.Status.CREATED));
		Page<Share> page = shareService2.findAll(filters, pageParam.getPageable(Share.class));
		return Gsons.filterByFields(ShareDto.Brief.class, pageParam.getFields()).toJson(PageDto.of(page, ShareDto.toBrief));
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam("recipientType") String recipientType, @RequestParam(required = false) Long recipientId, PageParam pageParam) {
		Pageable pageable = pageParam.getPageable(Share.class);
		Page<Share> page = null;
		if (recipientId == null){
			page = shareService2.findAll(currentUserId(), recipientType, pageable);
		} else {
			page = shareService2.findAll(currentUserId(), recipientType, recipientId, pageable);
		}
		return Gsons.filterByFields(ShareDto.Brief.class, pageParam.getFields()).toJson(PageDto.of(page, ShareDto.toBrief));
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String create(@Valid CreateRequest createRequest) {
		File file = fileService.find(createRequest.fileId);
		if (file == null) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		// 判断操作人员是否有权限
		if (!file.getOwner().getId().equals(currentUserId())) {
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		// 执行分享操作
		List<ShareRecipient> recipients = Lists.newArrayList();
		for (String recipient : createRequest.recipients) {
			ShareRecipient tmp = ShareDto.dtoToRecipient.apply(recipient);
			if (tmp != null) {
				recipients.add(tmp);
			}
		}
		Share share = shareService2.createShare(currentUser(), file, recipients, createRequest.shareHeadVersion, createRequest.message);

		// 推送消息
		String message = new StringBuilder().append(getUserPrincipal().getUserName()).append("给您分享了文件").append(file.getIsDir() ? "夹:" : ":")
				.append(file.getName()).toString();
		for (ReceivedShare receivedShare : receivedShareService.findAll(share)) {
			try {
				sendNotif(new Notification(NotifType.SHARE_CREATE, message, ReceivedShareDto.toNotify.apply(receivedShare),
						ImmutableList.<Long> of(receivedShare.getRecipient().getId())));
			} catch (Exception e) {
				_logger.error(null, e);
			}
		}
		return gson().toJson(ShareDto.toBrief.apply(share));
	}

	/**
	 * 搜索我搜到的分享
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "/received", method = RequestMethod.GET)
	@ResponseBody
	public String pageReceived(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		filters.add(new SearchFilter("recipient.id", Operator.EQ, currentUserId()));
		filters.add(new SearchFilter("isDeleted", Operator.EQ, Boolean.FALSE));
		filters.add(new SearchFilter("share.status", Operator.NE, Share.Status.CANCELED));
		Page<ReceivedShare> page = receivedShareService.findAll(filters, pageParam.getPageable(ReceivedShare.class));
		return Gsons.filterByFields(ReceivedShareDto.Brief.class, pageParam.getFields()).toJson(PageDto.of(page, ReceivedShareDto.toBrief));
	}

	@RequestMapping(value = "/received/search", method = RequestMethod.GET)
	@ResponseBody
	public String searchReceived(@RequestParam("recipientType") String recipientType, @RequestParam(required = false) Long recipientId,
			PageParam pageParam) {
		Pageable pageable = pageParam.getPageable(ReceivedShare.class);
		Page<ReceivedShare> page;
		if (recipientId == null) {
			page = receivedShareService.findAll(currentUserId(), recipientType, pageable);
		} else {
			page = receivedShareService.findAll(currentUserId(), recipientType, recipientId, pageable);
		}
		return Gsons.filterByFields(ReceivedShareDto.Brief.class, pageParam.getFields()).toJson(PageDto.of(page, ReceivedShareDto.toBrief));
	}
}
