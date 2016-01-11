//package cn.ac.iscas.oncecloudshare.service.service.common;
//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.IOException;
//import java.net.URL;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import javax.annotation.Resource;
//
//import net.lingala.zip4j.core.ZipFile;
//import net.lingala.zip4j.exception.ZipException;
//
//import org.apache.commons.io.FileUtils;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//import org.springside.modules.utils.Collections3;
//
//import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
//import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
//import cn.ac.iscas.oncecloudshare.service.dto.common.ExtensionDto;
//import cn.ac.iscas.oncecloudshare.service.model.common.ExtensionInfo;
//import cn.ac.iscas.oncecloudshare.service.system.ServerInfo;
//import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager;
//import cn.ac.iscas.oncecloudshare.service.system.extension.ExtensionManager.ExtensionHolder;
//import cn.ac.iscas.oncecloudshare.service.utils.DateUtils;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.io.Files;
//import com.google.common.io.Resources;
//
//@Component
//public class ExtensionCenterService {
//	private RestTemplate restTemplate = new RestTemplate();
//	
//	@Resource(name="globalConfigService")
//	private ConfigService<?> configService;
//
//	@Resource
//	private ExtensionManager extensionManager;
//
//	public ExtensionInfo findOne(long id) {
//		String q = "id::eq::" + id;
//		PageDto<ExtensionInfo> pageDto = findAll(q, new PageParam());
//		if (Collections3.isEmpty(pageDto.entries)) {
//			return null;
//		}
//		return pageDto.entries.get(0);
//	}
//
//	public PageDto<ExtensionInfo> findAll(String q, PageParam page) {
//		Map<String, String> varMap = Maps.newHashMap();
//		varMap.put("q", q);
//		varMap.put("pageSize", page.getPageSize().toString());
//		varMap.put("page", page.getPage().toString());
//		varMap.put("sort", page.getSort());
//		String url = getExtensionCenterUrl() + "/api/extensions?q={q}&pageSize={pageSize}&page={page}&sort={sort}";
//		String result = restTemplate.getForObject(url, String.class, varMap);
//		PageDto<ExtensionInfo> pageDto = ExtensionDto.pageDecoder.apply(result);
//		Collection<ExtensionHolder> currentExtensions = extensionManager.findAll();
//		String buildNumber = ServerInfo.getBuildNumber();
//		for (ExtensionInfo dto : pageDto.entries) {
//			for (ExtensionHolder holder : currentExtensions) {
//				if (dto.name.equals(holder.name) && dto.version.equals(holder.version)) {
//					dto.installed = true;
//					dto.enabled = holder.enabled;
//				}
//			}
//			// 查看是否支持
//			dto.supported = true;
//			if (dto.minSupport != null && buildNumber.compareTo(dto.minSupport) < 0) {
//				dto.supported = false;
//			}
//			if (dto.maxSupport != null && buildNumber.compareTo(dto.maxSupport) > 0) {
//				dto.supported = false;
//			}
//		}
//		return pageDto;
//	}
//
//	public void install(long id) throws IOException, ZipException {
//		URL source = new URL(getExtensionCenterUrl() + "/api/extensions/" + id + "?download");
//		String extractName = UUID.randomUUID().toString().replaceAll("-", "");
//		String fileName = extractName.concat(".zip");
//		File extractFile = new File(FileUtils.getTempDirectoryPath(), extractName);
//		File file = new File(FileUtils.getTempDirectoryPath(), fileName);
//		try {
//			FileUtils.copyURLToFile(source, file, (int) DateUtils.MILLIS_PER_MINUTE, (int) DateUtils.MILLIS_PER_MINUTE);
//			ZipFile zipFile = new ZipFile(file);
//			zipFile.extractAll(extractFile.getAbsolutePath());
//			// 拷贝文件
//			File configsFile = null;
//			File extensionFile = null;
//			List<File> jarFiles = Lists.newArrayList();
//			File configsFolder = new File(Resources.getResource("extension-configs").getFile());
//			File extensionsFolder = new File(Resources.getResource("extensions").getFile());
//			File jarFolders = new File(extensionsFolder.getParentFile().getParentFile(), "lib");
//			jarFolders.mkdirs();
//			for (File tmpFile : extractFile.listFiles()) {
//				String tmpName = tmpFile.getName().toLowerCase();
//				if (tmpFile.isFile()) {
//					if (tmpName.endsWith("configs.properties")) {
//						configsFile = tmpFile;
//					} else if (tmpName.endsWith(".properties")) {
//						extensionFile = tmpFile;
//					}
//				} else {
//					if (tmpName.equals("lib")) {
//						for (File jarFile : tmpFile.listFiles(new FileFilter() {
//							@Override
//							public boolean accept(File pathname) {
//								return pathname.getName().toLowerCase().endsWith(".jar");
//							}
//						})) {
//							jarFiles.add(jarFile);
//						}
//					}
//				}
//			}
//			if (extensionFile == null) {
//				throw new IOException();
//			}
//			Files.copy(extensionFile, new File(extensionsFolder, extensionFile.getName()));
//			if (configsFile != null) {
//				Files.copy(configsFile, new File(configsFolder, configsFile.getName()));
//			}
//			for (File from : jarFiles) {
//				File to = new File(jarFolders, from.getName());
//				if (!to.exists()) {
//					Files.copy(from, to);
//				}
//			}
//		} finally {
//			file.delete();
//			extractFile.delete();
//		}
//	}
//
//	public String getExtensionCenterUrl() {
//		return configService.getConfig(Configs.Keys.EXT_CENTER_URL, null);
//	}
//
//	public String getExtensionCenterUser() {
//		return configService.getConfig(Configs.Keys.EXT_CENTER_USER, null);
//	}
//
//	public String getExtensionCenterPass() {
//		return configService.getConfig(Configs.Keys.EXT_CENTER_PASS, null);
//	}
//}
