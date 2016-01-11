package cn.ac.iscas.oncecloudshare.service.controller.v2.share;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.dto.share.UserShareDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.share.UserShare;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FolderService;
import cn.ac.iscas.oncecloudshare.service.service.share.ShareService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/api/v2/usershares", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class UserShareController extends BaseController {
	static Logger _logger = LoggerFactory.getLogger(UserShareController.class);

	@Resource
	private FileService fileService;
	@Resource
	private ShareService shareService;
	@Resource
	private UserService userService;
	@Resource
	private FolderService folderService;

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public String list(@RequestParam(value = "q", required = false) String query, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(query) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(query);
		filters.add(new SearchFilter("owner.id", Operator.EQ, String.valueOf(currentUserId())));
		Page<UserShare> page = shareService.searchUserShare(filters, pageParam.getPageable(UserShare.class));
		return Gsons.filterByFields(UserShareDto.OwnerView.class, pageParam.getFields()).toJson(PageDto.of(page, UserShareDto.ownerViewTransformer));
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String create(UserShareDto.Create dto) {
		File file = dto.fileId == null ? null : fileService.find(dto.fileId);
		checkShare(file);

		List<UserShare> shares = new ArrayList<UserShare>();
		if (dto.recipients != null && dto.recipients.length > 0) {
			for (Long recipientId : dto.recipients) {
				if (recipientId.equals(currentUserId())) {
					continue;
				}
				try {
					User recipient = recipientId == null ? null : userService.find(recipientId);
					if (recipient != null) {
						UserShare userShare = shareService.findUserShareBy(dto.fileId, recipientId, currentUserId());
						if (userShare == null) {
							userShare = new UserShare();
							userShare.setCreateTime(new Date());
							userShare.setOwner(currentUser());
							userShare.setDescription(dto.description);
							userShare.setFile(file);
							if (!dto.shareHeadVersion && file.getHeadVersion() != null) {
								userShare.setFileVersion(file.getHeadVersion().getVersion());
							}
							userShare.setRecipient(recipient);
							shareService.saveUserShare(userShare);
						}
						shares.add(userShare);
					}
				} catch (Exception e) {
					_logger.error(null, e);
				}
			}
		}
		return gson().toJson(Lists.transform(shares, UserShareDto.ownerViewTransformer));
	}

	@RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
	@ResponseBody
	public String retrive(final HttpServletRequest request, @PathVariable("id") Long id) {
		UserShare userShare = shareService.getUserShareById(id);
		checkIsNull(userShare);
		// 如果是owner
		if (userShare.getOwner().getId().equals(currentUserId())) {
			return gson().toJson(UserShareDto.ownerViewTransformer.apply(userShare));
		}
		// 如果是recipient
		else if (userShare.getRecipient().getId().equals(currentUserId())) {
			return gson().toJson(UserShareDto.recipientViewTransformer.apply(userShare));
		}
		throw new RestException(ErrorCode.USERSHARE_FORBIDDEN);
	}

	@RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(final HttpServletRequest request, @PathVariable("id") Long id) {
		UserShare userShare = shareService.getUserShareById(id);
		checkIsNull(userShare);
		// 如果是owner
		if (userShare.getOwner().getId().equals(currentUserId()) || userShare.getRecipient().getId().equals(currentUserId())) {
			shareService.deleteUserShare(userShare);
			return gson().toJson(ResponseDto.OK);
		}
		throw new RestException(ErrorCode.USERSHARE_FORBIDDEN);
	}

	@RequestMapping(value = "received", method = RequestMethod.GET)
	@ResponseBody
	public String received(@RequestParam(value = "q", required = false) String query, PageParam pageParam) {
		List<SearchFilter> filters = StringUtils.isEmpty(query) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(query);
		filters.add(new SearchFilter("recipient.id", Operator.EQ, String.valueOf(currentUserId())));
		Page<UserShare> page = shareService.searchUserShare(filters, pageParam.getPageable(UserShare.class));
		return Gsons.filterByFields(UserShareDto.OwnerView.class, pageParam.getFields()).toJson(
				PageDto.of(page, UserShareDto.recipientViewTransformer));
	}

	@RequestMapping(value = "{id:\\d+}/content")
	public void content(@PathVariable("id") long shareId, @RequestParam(value = "fileId", required = false) Long fileId, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		UserShare userShare = shareService.getUserShareById(shareId);
		check(userShare, OPERATION_DOWNLOAD);
		if (userShare.getFile() != null) {
			// 如果是文件夹
			if (userShare.getFile().getIsDir()) {
				// 如果下载的是子文件
				if (fileId != null) {
					File file = fileService.find(fileId);
					checkIsNull(file);
					if (!file.isChildOf(userShare.getFile())){
						throw new RestException(ErrorCode.FORBIDDEN);
					}
					// 下载，不支持下载文件功能
					if (file.getIsDir()) {
						throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
					} else {
						downloadFileVersion(file.getHeadVersion(), request, response);
					}
				}
				// 如果下载的
				else {
					throw new RestException(ErrorCode.SERVICE_UNVAILABLE);
				}
			} else {
				downloadFileVersion(userShare.getSharedFileVersion(), request, response);
			}
		}
	}
	
	@RequestMapping(value = "{id:\\d+}/children", method = RequestMethod.GET)
	@ResponseBody
	public String getChildren(@PathVariable long shareId, @RequestParam(value = "folderId", required = false) Long folderId, PageParam pageParam) {
		UserShare userShare = shareService.getUserShareById(shareId);
		check(userShare, OPERATION_VIEW);
		File file = userShare.getFile();
		if (!file.getIsDir()){
			throw new RestException(404,"folder not exists");
		}
		File parent;
		// 如果是查询的子文件夹
		if (folderId != null){
			parent = folderService.find(folderId);
			if (parent == null){
				throw new RestException(404,"folder not exists");
			}
			if (!parent.isChildOf(file)){
				throw new RestException(ErrorCode.FORBIDDEN);
			}
		} else {
			parent = file;
		}
		Page<File> chilren = folderService.findChildren(parent.getId(), pageParam.getPageable(File.class));
		return Gsons.filterByFields(FileDto.class, pageParam.getFields()).toJson(PageDto.of(chilren, FileDto.TRANSFORMER));
	}

	// TODO: to reconstrcut

	protected File findFile(Long fileId) {
		File file = fileService.findFile(fileId);
		if (file == null) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		return file;
	}
	
	public void checkIsNull(File file){
		if (file == null){
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
	}

	public void checkIsNull(UserShare userShare) {
		if (userShare == null) {
			throw new RestException(ErrorCode.USERSHARE_NOT_FOUND);
		}
	}

	public void checkShare(File file) {
		if (file == null) {
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		if (!file.getOwner().getId().equals(currentUserId())) {
			throw new RestException(ErrorCode.FILE_NOT_MODIFIABLE);
		}
	}

	public void check(UserShare userShare, String operation) {
		checkIsNull(userShare);
		if (OPERATION_DOWNLOAD.equals(operation) || OPERATION_VIEW.equals(operation)) {
			if (!userShare.getOwner().getId().equals(currentUserId()) && userShare.getRecipient().getId().equals(currentUserId())) {
				throw new RestException(ErrorCode.USERSHARE_FORBIDDEN);
			}
		}
	}

	// 下载操作
	public static final String OPERATION_DOWNLOAD = "download";
	// 查看操作
	public static final String OPERATION_VIEW = "view";
}
