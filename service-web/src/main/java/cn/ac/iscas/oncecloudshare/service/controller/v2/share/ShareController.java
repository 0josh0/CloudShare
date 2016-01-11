package cn.ac.iscas.oncecloudshare.service.controller.v2.share;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.NotifType;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.share.ReceivedShareDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.share.ReceivedShareService;
import cn.ac.iscas.oncecloudshare.service.service.share.ShareService2;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.collect.ImmutableList;

/**
 * 提供我的分享的相关功能，包括：
 * 
 * <pre>
 * 撤销我的分享    DELETE		/api/v2/shares/{shareId}
 * </pre>
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Controller
@RequestMapping(value = "/api/v2/shares/{shareId:\\d+}", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ShareController extends BaseController {
	private static final Logger _logger = LoggerFactory.getLogger(ShareController.class);

	@Resource
	private ShareService2 shareService2;
	@Resource
	private FileService fileService;
	@Resource
	private ReceivedShareService receivedShareService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable("shareId") long shareId) {
		Share share = shareService2.findOne(currentUserId(), shareId);
		if (share == null) {
			throw new RestException(ErrorCode.USERSHARE_NOT_FOUND);
		}
		model.addAttribute("share", share);
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public String cancel(@ModelAttribute("share") Share share) {
		shareService2.cancelShare(share);
		// 推送消息
		String message = new StringBuilder().append(getUserPrincipal().getUserName()).append("取消了分享:").append(share.getFile().getName()).toString();
		for (ReceivedShare receivedShare : receivedShareService.findAll(share)) {
			try {
				sendNotif(new Notification(NotifType.SHARE_CANCEL, message, ReceivedShareDto.toNotify.apply(receivedShare),
						ImmutableList.<Long> of(receivedShare.getRecipient().getId())));
			} catch (Exception e) {
				_logger.error(null, e);
			}
		}

		return gson().toJson(ResponseDto.OK);
	}
}
