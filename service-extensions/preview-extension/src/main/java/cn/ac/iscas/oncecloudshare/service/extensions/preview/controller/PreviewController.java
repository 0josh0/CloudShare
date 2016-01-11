package cn.ac.iscas.oncecloudshare.service.extensions.preview.controller;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.preview.model.Preview;
import cn.ac.iscas.oncecloudshare.service.extensions.preview.service.PreviewService;
import cn.ac.iscas.oncecloudshare.service.extensions.preview.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService.ConfigUtil;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MimeTypes;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

/**
 * 提供分享中心的相关功能，包括：
 * 
 * <pre>
 * 预览    GET		/api/v2/exts/preview
 * 获取支持的后缀    GET		/api/v2/exts/preview/supports
 * </pre>
 * 
 * @author cly
 * @version
 * @since JDK 1.6
 */
@Controller
@RequestMapping("/api/v2/exts/preview")
public class PreviewController extends BaseController {
	private static final Logger _logger = LoggerFactory.getLogger(PreviewController.class);

	// 可以直接预览的文件的后缀
	public static final String CFG_NOCONVERT_EXTS = ConfigUtil.buildKey(Constants.DOMAIN, "NO_CONVERT_EXTS");
	private static final String DEFAULT_NOCONVERT_EXTS = "xml,html,htm,out,css,js,java,aspx,php,txt,ini,jsp,png,jpg,gif,bmp,jpeg,pdf";

	public static List<String> TXT_EXTS = ImmutableList.<String> of("txt", "xml", "html", "htm", "out", "css", "js", "java", "aspx", "php", "ini",
			"jsp");

	@Resource
	private PreviewService previewService;
	@Resource
	private FileStorageService fileStorageService;
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	@PostConstruct
	public void postConstruct() {
		_logger.info("初始化PreviewExtController成功。。。");
	}

	@RequestMapping(method = RequestMethod.GET)
	public void preview(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "md5") String md5,
			@RequestParam(value = "filename", required = false) String filename) {
		String extension = StringUtils.isEmpty(filename) ? "" : Files.getFileExtension(filename).toLowerCase();
		String outputExt = null;
		try {
			ByteSource source = null;
			if (ArrayUtils.indexOf(getNoConvertExts(), extension) > -1) {
				source = fileStorageService.retrieveFileContent(md5);
				outputExt = getNoConvertOutputExt(extension);
			} else {
				Preview preview = previewService.convert(md5, extension, "preview");
				source = fileStorageService.retrieveFileContent(preview.getOutput());
				outputExt = preview.getOutputFormat();
			}
			// 如果是文本形式的，判断文本的编码方式
			if ("txt".equals(outputExt)) {
				response.setCharacterEncoding(getCharset(source));
			}
			response.setContentType(MimeTypes.getMimeType(outputExt));
			initDownload(request, response, source, null);
		} catch (Exception e1) {
			_logger.error(null, e1);
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "supports", method = RequestMethod.GET)
	@ResponseBody
	public String supports() {
		Collection<String> supports = new ArrayList<String>(previewService.getSupports("preview"));
		for (String ext : getNoConvertExts()) {
			supports.add(ext);
		}
		return gson().toJson(supports);
	}

	public String[] getNoConvertExts() {
		String str = configService.getConfig(CFG_NOCONVERT_EXTS, DEFAULT_NOCONVERT_EXTS);
		return str.split(",");
	}

	public String getNoConvertOutputExt(String inputExt) {
		if (TXT_EXTS.indexOf(inputExt) > -1) {
			return TXT_EXTS.get(0);
		}
		return inputExt;
	}

	/***
	 * 
	 * @param file
	 * @return
	 */
	public static String getCharset(ByteSource source) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		BufferedInputStream bis = null;
		try {
			bis = source.openBufferedStream();
			boolean checked = false;
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked) {
				while ((read = bis.read()) != -1) {
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF) (0x80
															// - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}
		} catch (Exception e) {
			_logger.error(null, e);
		} finally {
			IOUtils.closeQuietly(bis);
		}
		return charset;
	}
}
