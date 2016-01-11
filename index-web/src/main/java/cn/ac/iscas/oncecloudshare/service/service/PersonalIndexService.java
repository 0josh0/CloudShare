package cn.ac.iscas.oncecloudshare.service.service;

import javax.annotation.Resource;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.component.AWriterFactory;
import cn.ac.iscas.oncecloudshare.service.extensions.index.model.IndexEntity;

@Service
public class PersonalIndexService extends AIndexService {

	@Resource(name = "personalFactory")
	private AWriterFactory writerFactory;

	@Override
	public void updateIndex(IndexEntity<?> entity) throws Exception {

		Document doc = this.createDocument(entity);
		Term term = new Term("fileId", entity.getFileId().toString());
		IndexWriter indexWriter = writerFactory.obtainWriter(entity.getTenantId());
		indexWriter.updateDocument(term, doc);
		indexWriter.commit();;

	}

	@Override
	public void deleteIndex(IndexEntity<?> entity) throws Exception {
		Term term = new Term("fileId", entity.getFileId().toString());
		Query query = new TermQuery(term);
		IndexWriter indexWriter = writerFactory.obtainWriter(entity.getTenantId());
		indexWriter.deleteDocuments(query);
		indexWriter.forceMergeDeletes();
		indexWriter.commit();

	}

}
