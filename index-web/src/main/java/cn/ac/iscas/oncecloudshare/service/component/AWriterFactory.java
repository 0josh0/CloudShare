package cn.ac.iscas.oncecloudshare.service.component;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author One
 * 
 *         lucene IndexWriter 工厂类父类
 * 
 */
public abstract class AWriterFactory {

	protected static final int MAX_CACHE_SIZE = 100;

	protected IndexWriterConfig writerConfig;

	protected Analyzer analyzer = new IKAnalyzer();


	/**
	 * 索引 indexwriter 缓存
	 */
	protected LoadingCache<Long, IndexWriter> writerCache = CacheBuilder.newBuilder().softValues().maximumSize(MAX_CACHE_SIZE).build(new CacheLoader<Long, IndexWriter>() {

		@Override
		public IndexWriter load(Long key) throws Exception {
			File dirFile = ensureDir(key);
			Directory dir = FSDirectory.open(dirFile);
			return new IndexWriter(dir, writerConfig);
		}
	});

	public abstract IndexWriter obtainWriter(Long tenantId) throws Exception;

	protected abstract File ensureDir(Long tenantId);

}
