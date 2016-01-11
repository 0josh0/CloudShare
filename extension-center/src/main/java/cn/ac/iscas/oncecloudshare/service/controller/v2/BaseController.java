package cn.ac.iscas.oncecloudshare.service.controller.v2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.ByteRange;
import cn.ac.iscas.oncecloudshare.service.utils.http.MimeTypes;

import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

public class BaseController {
	private static final Logger _logger = LoggerFactory.getLogger(BaseController.class);

	protected Gson gson = Gsons.defaultGson();

	@Autowired
	protected ConfigService cService;

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

	protected HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	@SuppressWarnings("unchecked")
	protected <T> T modelAttribute(Model model, String attributeName) {
		return (T) model.asMap().get(attributeName);
	}

	protected void initDownload(HttpServletRequest request, HttpServletResponse response, ByteSource source, String filename) throws IOException {
		response.reset();
		String range = request.getHeader("Range");

		if (Strings.isNullOrEmpty(range)) {
			// full download
			response.addHeader(HttpHeaders.CONTENT_LENGTH, "" + source.size());
			if (filename != null) {
				response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + encodeFilename(request.getHeader("User-Agent"), filename));
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
		} catch (IOException e) {
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	protected String encodeFilename(String userAgent, String filename) {
		if (StringUtils.isEmpty(filename)) {
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
			_logger.error(null, e);
			return null;
		}
	}
}
