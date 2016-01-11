package cn.ac.iscas.oncecloudshare.service.extensions.preview.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.extensions.preview.FileConverter;
import cn.ac.iscas.oncecloudshare.service.extensions.preview.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService.ConfigUtil;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeException;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

@Component
public class OpenOfficeConverter implements FileConverter {
	private static final Logger _logger = LoggerFactory.getLogger(OpenOfficeConverter.class);

	/**
	 * open office服务host
	 */
	public static final String CFG_OPENOFFICE_HOST = ConfigUtil.buildKey(Constants.DOMAIN, "OPENOFFICE_HOST");
	/**
	 * open office服务端口
	 */
	public static final String CFG_OPENOFFICE_PORT = ConfigUtil.buildKey(Constants.DOMAIN, "OPENOFFICE_PORT");
	/**
	 * open office的安装路径
	 */
	public static final String CFG_OPENOFFICE_HOME = ConfigUtil.buildKey(Constants.DOMAIN, "OPENOFFICE_PATH");

	public static final String DEFAULT_OPENOFFICE_HOST = "127.0.0.1";
	public static final int DEFAULT_OPENOFFICE_PORT = 8100;
	public static final String DEFAULT_OPENOFFICE_HOME = "C:\\Program Files (x86)\\OpenOffice 4";
	
	public static void main(String[] args) {
		System.out.println(CFG_OPENOFFICE_HOME);
	}

	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	//
	private String openOfficeHost;
	private int openOfficePort;
	private String openofficeHome;

	private DocumentFormatRegistry documentFormatRegistry = new DefaultDocumentFormatRegistry();

	// 默认的输出格式为pdf格式
	private DocumentFormat outputFormat = guessDocumentFormat("pdf");

	@PostConstruct
	public void start() {
		openOfficeHost = configService.getConfig(CFG_OPENOFFICE_HOST, DEFAULT_OPENOFFICE_HOST);
		openOfficePort = configService.getConfigAsInteger(CFG_OPENOFFICE_PORT, DEFAULT_OPENOFFICE_PORT);
		openofficeHome = configService.getConfig(CFG_OPENOFFICE_HOME, DEFAULT_OPENOFFICE_HOME);
		// openofficeHome = openofficeHome.replaceAll("\\\\", "/");
		if (!openofficeHome.endsWith("\\")) {
			openofficeHome = openofficeHome.concat("\\");
		}
		// 尝试连接open office
		OpenOfficeConnection connection = new SocketOpenOfficeConnection(openOfficeHost, openOfficePort);
		try {
			connection.connect();
			connection.disconnect();
		} catch (ConnectException e) {
			_logger.error("连接OpenOffice服务失败，可能服务未启动", e);
			if (isLocalhost(openOfficeHost)) {
				String[] cmds = new String[] { openofficeHome + "program\\soffice.exe", "-headless",
						"-accept=\"socket,host=127.0.0.1,port=8100;urp;\"", "-nofirststartwizard" };
				try {
					Runtime.getRuntime().exec(cmds);
				} catch (IOException e1) {
					_logger.error("启动OpenOffice失败", e1);
				}
			}
		}
	}

	public void restart() {
		start();
	}

	/**
	 * 判断是否是本机
	 * 
	 * @param host
	 * @return
	 */
	private static boolean isLocalhost(String host) {
		return "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host);
	}

	/***
	 * 检测当前操作系统 1：window；2：Linux
	 * 
	 * @return
	 */
	public int checkOpSys() {
		int sys = 0;
		if (System.getProperty("os.name").split("indow").length > 1) {
			sys = 1;
		} else {
			sys = 2;
		}
		return sys;
	}

	/***
	 * 得到openoffice的安装路径
	 * 
	 * @return
	 */
	public String getOpenOfficePathUrl() {
		return configService.getConfig(Constants.DOMAIN, CFG_OPENOFFICE_HOME);
	}

	@Override
	public String[] getSupports() {
		return new String[] { "doc", "docx", "xls", "xlsx", "ppt", "pptx", "rtf", "wps" };
	}

	@Override
	public String getName() {
		return "openoffice converter";
	}

	/**
	 * 通过文件的扩展名获取文件格式
	 * 
	 * @param extension
	 * @return
	 */
	protected DocumentFormat guessDocumentFormat(String extension) {
		if ("wps".equalsIgnoreCase(extension)){
			return documentFormatRegistry.getFormatByFileExtension("doc");
		}
		return documentFormatRegistry.getFormatByFileExtension(extension);
	}

	@Override
	public String convert(InputStream input, String inputExtension, OutputStream output) throws Exception {
		OpenOfficeConnection connection = new SocketOpenOfficeConnection(openOfficeHost, openOfficePort);
		try {
			connection.connect();
			DocumentConverter converter = new OpenOfficeDocumentConverter(connection, documentFormatRegistry);
			converter.convert(input, guessDocumentFormat(inputExtension), output, outputFormat);
			connection.disconnect();
			return outputFormat.getFileExtension();
		} catch (java.net.ConnectException e) {
			_logger.error("pdf转换异常，openoffice服务未启动！", e);
			throw e;
		} catch (OpenOfficeException e) {
			_logger.error("****swf转换器异常，读取转换文件失败****", e);
			throw e;
		}
	}
}
