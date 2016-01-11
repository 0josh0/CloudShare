package cn.ac.iscas.oncecloudshare.service.extensions.preview.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.extensions.preview.FileConverter;
import cn.ac.iscas.oncecloudshare.service.extensions.preview.dao.PreviewDao;
import cn.ac.iscas.oncecloudshare.service.extensions.preview.model.Preview;
import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;

import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

@Service
@Transactional(readOnly = true)
public class PreviewService {
	@Resource
	private PreviewDao previewDao;
	@Resource
	private FileStorageService fileStorageService;
	// 锁
	private LockSet<String> lockSet = new LockSet<String>();

	private Map<String, Map<String, FileConverter>> fileConverts = Maps.newConcurrentMap();
	@Resource
	private OpenOfficeConverter openOfficeConverter;
	@Resource
	private FfmpegAudioConverter ffmpegAudioConverter;
	
	@PostConstruct
	public void init(){
		addFileConverter("preview", openOfficeConverter);
		addFileConverter("preview", ffmpegAudioConverter);
	}
	
	protected FileConverter getFileConverter(String type, String extension){
		Map<String, FileConverter> map = fileConverts.get(type);
		return map == null ? null : map.get(extension);
	}
	
	public void addFileConverter(String type, FileConverter converter){
		Map<String, FileConverter> map = fileConverts.get(type);
		if (map == null){
			map = Maps.newConcurrentMap();
			fileConverts.put(type, map);
		}
		for (String support : converter.getSupports()){
			map.put(support, converter);
		}
	}

	public Preview convert(String md5, String extension, String type) throws Exception {
		FileConverter converter = getFileConverter(type, extension);
		if (converter == null) {
			// 尝试寻找以前同样的md5对应的预览
			List<Preview> previews = previewDao.findAll(md5, type);
			if (previews != null && previews.size() > 0) {
				return previews.get(0);
			}
			throw new UnsupportedOperationException();
		}
		// 如果当前正在生成预览文件，等待生成完了之后再返回
		Lock lock = lockSet.getLock(generateLockKey(md5, type, converter.getName()));
		lock.lock();
		try {
			Preview preview = previewDao.findOne(md5, type, converter.getName());
			if (preview != null) {
				return preview;
			} else {
				return internalConvert(md5, extension, type, converter);
			}
		} finally {
			lock.unlock();
		}
	}

	@Transactional(readOnly = false)
	protected Preview internalConvert(String md5, String extension, String type, FileConverter converter) throws Exception {
		InputStream input = null;
		File tmpFile = new File(System.currentTimeMillis() + ".tmp");
		OutputStream output = null;
		try {
			ByteSource src = fileStorageService.retrieveFileContent(md5);
			input = src.openStream();
			output = new FileOutputStream(tmpFile);
			String outputFormat = converter.convert(input, extension, output);
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			// 保存转换后的文件
			input = new FileInputStream(tmpFile);
			FileSource fileSource = fileStorageService.saveFile(Files.asByteSource(tmpFile));
			// 将结果保存到数据库
			Preview preview = new Preview();
			preview.setInput(md5);
			preview.setOutput(fileSource.getMd5());
			preview.setOutputFormat(outputFormat);
			preview.setConverterType(type);
			preview.setConverter(converter.getName());
			previewDao.save(preview);
			
			return preview;
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			tmpFile.delete();
		}
	}

	protected String generateLockKey(String md5, String type, String converter) {
		return md5 + "_" + type + "_" + converter;
	}

	public Collection<String>  getSupports(String type) {
		Map<String, FileConverter> map = fileConverts.get(type);
		return map == null ? new HashSet<String>(0) : map.keySet();
	}
}