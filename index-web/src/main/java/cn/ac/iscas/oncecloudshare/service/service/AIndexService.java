package cn.ac.iscas.oncecloudshare.service.service;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.tools.ExcelReader;
import cn.ac.iscas.oncecloudshare.service.tools.GetEncoding;
import cn.ac.iscas.oncecloudshare.service.tools.PDFReader;
import cn.ac.iscas.oncecloudshare.service.tools.PptReader;
import cn.ac.iscas.oncecloudshare.service.tools.WordReader;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.io.ByteSource;
import com.google.gson.Gson;

@SuppressWarnings("deprecation")
public abstract class AIndexService {

	protected static final Logger _logger = LoggerFactory.getLogger(AIndexService.class);

	protected static final Gson GSON = Gsons.defaultGson();

	@Resource
	protected RuntimeContext runtimeContext;

	@Autowired
	protected FileService fileService;

	/**
	 * 查找目标文件夹，检查是否存在
	 * 
	 * @param fileId
	 * @return
	 */
	protected File findFile(Long fileId) {
		File file = fileService.findFile(fileId);
		if (file == null)
			throw new NullPointerException();
		return file;
	}

	private ByteSource openStream(String md5) throws IOException {
		return runtimeContext.getFileStorageService().retrieveFileContent(md5);
	}

	public abstract void updateIndex(IndexEntity<?> entity) throws Exception;

	public abstract void deleteIndex(IndexEntity<?> entity) throws Exception;

	protected Document createDocument(IndexEntity<?> entity) throws Exception {

		Document document = new Document();

		String fileid = entity.getFileId().toString();

		String fileName = entity.getFileName();

		String contents = getFileContent(entity);

		document.add(new Field("fileId", fileid, Store.YES, Index.NOT_ANALYZED));
		if (!StringUtils.isEmpty(contents)) {
			document.add(new Field("contents", contents, Store.YES, Index.ANALYZED));
		}

		document.add(new Field("name", fileName, Store.YES, Index.ANALYZED));
		document.add(new Field("meta", entity.getJsonObject(), Store.YES, Index.NOT_ANALYZED));
		document.add(new Field("owner", entity.getFileOwner().toString(), Store.YES, Index.NOT_ANALYZED));

		return document;

	}

	protected String getFileContent(IndexEntity<?> entity) throws Exception {

		String type = entity.getFileType();

		InputStream ins = openStream(entity.getMd5()).openStream();

		String contents = "";
		if (ins == null)
			return contents;

		try {
			// pdf
			if (type.equalsIgnoreCase("pdf")) {
				contents = PDFReader.getPDFtext(ins);
			}
			// excel 2003
			else if (type.equalsIgnoreCase("xls")) {
				contents = ExcelReader.getExcelText2003(ins);
			}
			// excel 2007
			else if (type.equalsIgnoreCase("xlsx")) {
				contents = ExcelReader.getExcelText2007(ins);
			}
			// word 2003
			else if (type.equalsIgnoreCase("doc")) {
				contents = WordReader.getWordText2003(ins);
			}
			// word 2007
			else if (type.equalsIgnoreCase("docx")) {
				contents = WordReader.getWordText2007(ins);
			}

			// ppt 2003
			else if (type.equalsIgnoreCase("ppt")) {
				contents = PptReader.getPttText2003(ins);
			}
			// ppt 2007
			else if (type.equalsIgnoreCase("pptx")) {
				contents = PptReader.getPttText2007(ins);
			}

			// txt html xml ..
			else if (type.equalsIgnoreCase("txt") || type.equalsIgnoreCase("html") || type.equalsIgnoreCase("xml") || type.equalsIgnoreCase("shtml")) {

				String encoding = GetEncoding.getCharset(ins);
				contents = IOUtils.toString(openStream(entity.getMd5()).openStream(), encoding).trim();
			}
		} finally {
			ins.close();
		}
		return contents;
	}
}
