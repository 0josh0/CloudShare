package cn.ac.iscas.oncecloudshare.service.component;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.core.Reader;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;

@Component
public class PersonalReaderFactory implements Reader {

	@Value("${personalDir}")
	private String indexDir;

	@Resource
	private TenantService tenantService;

	@Override
	public IndexReader createReader() throws IOException {

			Long tenantId = tenantService.getCurrentTenant().getId();

			File dir = new File(indexDir + "/" + tenantId);

			if (!dir.exists())
				dir.mkdirs();

			Directory directory = FSDirectory.open(dir);
			return DirectoryReader.open(directory);
		
	}
}
