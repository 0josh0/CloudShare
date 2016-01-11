package cn.ac.iscas.oncecloudshare.service.controller.v2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFileVersion;
import cn.ac.iscas.oncecloudshare.service.model.notif.Notification;
import cn.ac.iscas.oncecloudshare.service.model.notif.NotificationType;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.DefaultGlobalConfigService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.ByteRange;
import cn.ac.iscas.oncecloudshare.service.utils.http.MimeTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

public class BaseController {

	protected final ResponseEntity<Object> OK = new ResponseEntity<Object>(HttpStatus.OK);
	protected final ResponseEntity<Object> NOT_MODIFIED = new ResponseEntity<Object>(HttpStatus.NOT_MODIFIED);

	protected Gson gson = Gsons.defaultGson();

	@Autowired
	protected RuntimeContext runtimeContext;

	/**
	 * 系统全局配置
	 */
	@Resource(name="globalConfigService")
	protected DefaultGlobalConfigService globalConfigService;

	@Autowired
	protected UserService uService;

	protected Gson gson() {
		return Gsons.defaultGson();
	}

	/**
	 * 正确返回
	 * 
	 * @return
	 */
	protected String ok() {
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 当前接口调用者的身份
	 * 
	 * @return
	 */
	protected Object getPrincipal() {
		return SecurityUtils.getSubject().getPrincipal();
	}

	/**
	 * 如果不是UserPrincipal，将返回null
	 * 
	 * @return
	 */
	protected UserPrincipal getUserPrincipal() {
		Object principal = getPrincipal();
		return (principal instanceof UserPrincipal) ? (UserPrincipal) principal : null;
	}

	/**
	 * 当前用户，如果没登陆，将返回null
	 * 
	 * @return
	 */
	protected User currentUser() {
		Long userId = currentUserId();
		return userId == null ? null : uService.findExistingUser(userId);
	}

	/**
	 * 当前用户id，如果没登陆，将返回null
	 * 
	 * @return
	 */
	protected Long currentUserId() {
		UserPrincipal principal = getUserPrincipal();
		return principal == null ? null : getUserPrincipal().getUserId();
	}

	/**
	 * 是否是已登录的用户
	 * 
	 * @return
	 */
	protected boolean isAuthenticatedUser() {
		return getUserPrincipal() != null;
	}

	protected void postEvent(Object event) {
		runtimeContext.getEventBus().post(event);
	}

	protected void sendNotif(Notification notification) {
		runtimeContext.getNotifService().sendNotif(notification);
	}
	

	protected void sendNotif(NotificationType type, String content, Object attributes, List<Long> to) {
		runtimeContext.getNotifService().sendNotif(new Notification(type, content, attributes, to));
	}

	protected void sendNotif(NotificationType type, String content, Object attributes, Long to) {
		runtimeContext.getNotifService().sendNotif(new Notification(type, content, attributes, to));
	}

	protected HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
	
	/**
	 * 下载文件版本
	 * 
	 * @param fv
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void downloadFileVersion(GenericFileVersion<?> fv, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (fv == null) {
			throw new RestException(ErrorCode.FILE_VERSION_NOT_FOUND);
		}
		ByteSource source = runtimeContext.getFileStorageService().retrieveFileContent(fv.getMd5());
		initDownload(request, response, source, fv.getFile().getName());
	}

	protected String encodeFilename(String userAgent, String filename) {
		if (Strings.isNullOrEmpty(filename) || Strings.isNullOrEmpty(filename)) {
			return filename;
		}
		try {
			userAgent = userAgent.toLowerCase();
			String utf8Encoded = URLEncoder.encode(filename, "utf-8");
			// IE浏览器，只能采用URLEncoder编码
			if (userAgent.indexOf("msie") != -1) {
				return "filename=\"" + utf8Encoded + "\"";
			}
			// Opera浏览器只能采用filename*
			else if (userAgent.indexOf("opera") != -1) {
				return "filename*=UTF-8''" + utf8Encoded;
			}
			// Safari浏览器，只能采用ISO编码的中文输出
			else if (userAgent.indexOf("safari") != -1) {
				return "filename=\"" + new String(filename.getBytes("UTF-8"), "ISO8859-1") + "\"";
			}
			// Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
			else if (userAgent.indexOf("applewebkit") != -1) {
				utf8Encoded = MimeUtility.encodeText(filename, "UTF8", "B");
				return "filename=\"" + utf8Encoded + "\"";
			}
			// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
			else if (userAgent.indexOf("mozilla") != -1) {
				return "filename*=UTF-8''" + utf8Encoded;
			}
			return "filename*=UTF-8''" + utf8Encoded;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param request
	 * @param response
	 * @param source
	 * @param filename
	 *            文件名，可为null
	 * @throws IOException
	 */
	protected void initDownload(HttpServletRequest request, HttpServletResponse response, ByteSource source, String filename)
			throws IOException {
		// response.reset();
		String range = request.getHeader("Range");

		if (Strings.isNullOrEmpty(range)) {
			// full download
			response.addHeader(HttpHeaders.CONTENT_LENGTH, "" + source.size());
			if (filename != null) {
				response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; " + encodeFilename(request.getHeader("User-Agent"), filename));
			}
		} else {
			// range download
			ByteRange byteRange = null;
			try {
				byteRange = ByteRange.getRangeInstance(range, source.size());
			} catch (RuntimeException e) {
				response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
				response.setDateHeader("Date", Calendar.getInstance().getTimeInMillis());
				return;
			}
			long length = byteRange.getEndByte() - byteRange.getStartByte() + 1;
			long totalSize = source.size();
			source = source.slice(byteRange.getStartByte(), length);

			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			response.setDateHeader("Date", Calendar.getInstance().getTimeInMillis());
			// response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
			// "attachment; filename=\""+filename+"\"");
			String rangeHeader = "Content-Range: bytes " + byteRange.getStartByte() + "-" + byteRange.getEndByte() + "/" + totalSize + "";

			response.addHeader(HttpHeaders.CONTENT_RANGE, rangeHeader);
			response.setContentLength((int) length);
		}
		if (filename != null) {
			response.setContentType(MimeTypes.getMimeTypeByFilename(filename));
		}

		OutputStream out = response.getOutputStream();
		try {
			source.copyTo(response.getOutputStream());
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	protected boolean matchETagStrong(HttpServletRequest request, String eTagWithoutQuota) {
		String requestETag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		String eTag = eTagWithoutQuota = "\"" + eTagWithoutQuota + "\"";
		if (eTag.equals(requestETag)) {
			return true;
		}
		return false;
	}

	protected void setETag(HttpServletResponse response, String eTagWithoutQuota) {
		response.setHeader(HttpHeaders.ETAG, "\"" + eTagWithoutQuota + "\"");
	}

	/**
	 * 将搜索参数转变为查询数据库用的searchfilter
	 * 
	 * @param q
	 * @return
	 */
	protected List<SearchFilter> decodeFilters(String q) {
		if (Strings.isNullOrEmpty(q)) {
			return Lists.newArrayList();
		}
		return SearchFilter.parseQuery(q);
	}
}
