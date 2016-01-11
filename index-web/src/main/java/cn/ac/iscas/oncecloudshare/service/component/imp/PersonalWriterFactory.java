package cn.ac.iscas.oncecloudshare.service.component.imp;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.component.AWriterFactory;

@Component(value = "personalFactory")
public class PersonalWriterFactory extends AWriterFactory {

	@Value("${personalDir}")
	private String rootDir;

	@PostConstruct
	public void init() {

		this.writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);

		this.writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
	}

	@Override
	public IndexWriter obtainWriter(Long tenantId) throws Exception  {


		if (tenantId == null)
			throw new RuntimeException();

		IndexWriter indexWriter = writerCache.get(tenantId);
		
		return indexWriter;
	}

	@Override
	protected File ensureDir(Long tenantId) {

		File dirFile = new File(this.rootDir, tenantId.toString());

		if (!dirFile.exists())
			dirFile.mkdirs();

		return dirFile;
	}

	public static void main(String[] args) {

		File file = new File("e:/11/11/11");
		file.mkdirs();
		System.out.println(file.isDirectory());
	}

}
