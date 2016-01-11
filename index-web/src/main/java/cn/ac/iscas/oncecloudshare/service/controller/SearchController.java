package cn.ac.iscas.oncecloudshare.service.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wltea.analyzer.lucene.IKAnalyzer;

import cn.ac.iscas.oncecloudshare.service.component.FilterBuilder;
import cn.ac.iscas.oncecloudshare.service.component.PersonalReaderFactory;
import cn.ac.iscas.oncecloudshare.service.component.SpaceReaderFactory;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.FileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.collect.Lists;

/**
 * @author One
 * 
 */
@Controller
@RequestMapping(value = "/api/v2/users/index", produces = { MediaTypes.JSON_UTF8, MediaTypes.TEXT_PLAIN_UTF8 })
public class SearchController extends BaseController {

	// private static final String TEANT_ID_PARAM = "x-tenant-id";

	private static final String[] FIELDS = { "contents", "name", };

	private Analyzer analyzer = new IKAnalyzer();

	@Resource
	private PersonalReaderFactory pFactory;

	@Resource
	private SpaceReaderFactory sFactory;

	@Resource
	private FilterBuilder filterBuilder;

	private static final SearcherFactory searcherFactory = new SearcherFactory();

	@ResponseBody
	@RequestMapping(value = "/personal", method = RequestMethod.GET)
	public String searchPersonal(@RequestParam String keyWord, PageParam pageParam) throws IOException, ParseException {

		PageDto<FileDto> resultPage = new PageDto<>();

		IndexReader reader = null;
		try {
			reader = pFactory.createReader();

			QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_40, FIELDS, analyzer);

			Query query = queryParser.parse(keyWord);

			ChainedFilter filter = filterBuilder.createrFilter(new String[] { currentUserId().toString() });

			IndexSearcher indexSearcher = searcherFactory.newSearcher(reader);

			TopDocs topDocs = indexSearcher.search(query, filter, 10000);// 只返回前100条记录

			int totalCount = topDocs.totalHits; // 搜索结果总数量

			ScoreDoc[] scoreDocs = topDocs.scoreDocs; // 搜索返回的结果集合

			int currentPage = pageParam.getPage();

			int pageSize = pageParam.getPageSize();

			int totalPages = totalCount / pageSize + 1;

			List<FileDto> results = Lists.newArrayList();
			// 查询起始记录位置
			int begin = pageSize * (currentPage);
			// 查询终止记录位置
			int end = Math.min(begin + pageSize, scoreDocs.length);

			// 进行分页查询
			for (int i = begin; i < end; i++) {
				int docID = scoreDocs[i].doc;
				Document doc = indexSearcher.doc(docID);
				String contents = doc.get("meta");
				results.add(gson.fromJson(contents, FileDto.class));
			}

			resultPage.page = currentPage;
			resultPage.pageSize = pageSize;
			resultPage.totalPages = totalPages;
			resultPage.totalSize = totalCount;
			resultPage.entries = results;
		} catch (IndexNotFoundException e) {

		} finally {
			if (reader != null)
				reader.close();
		}
		return gson().toJson(resultPage);
	}

	@ResponseBody
	@RequestMapping(value = "/space", method = RequestMethod.GET)
	public String searchWorkspace(@RequestParam String keyWord, PageParam pageParam, @RequestParam Long workspaceId) throws IOException, ParseException {

		PageDto<SpaceFileDto> resultPage = new PageDto<>();
		IndexReader reader = null;

		try {

			reader = sFactory.createReader();

			QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_40, FIELDS, analyzer);

			Query query = queryParser.parse(keyWord);

			ChainedFilter filter = filterBuilder.createrFilter(new String[] { workspaceId.toString() });

			IndexSearcher indexSearcher = searcherFactory.newSearcher(reader);

			TopDocs topDocs = indexSearcher.search(query, filter, 10000);// 只返回前100条记录

			int totalCount = topDocs.totalHits; // 搜索结果总数量

			ScoreDoc[] scoreDocs = topDocs.scoreDocs; // 搜索返回的结果集合

			int currentPage = pageParam.getPage();

			int pageSize = pageParam.getPageSize();

			int totalPages = totalCount / pageSize + 1;

			List<SpaceFileDto> results = Lists.newArrayList();
			// 查询起始记录位置
			int begin = pageSize * (currentPage);
			// 查询终止记录位置
			int end = Math.min(begin + pageSize, scoreDocs.length);

			// 进行分页查询
			for (int i = begin; i < end; i++) {
				int docID = scoreDocs[i].doc;
				Document doc = indexSearcher.doc(docID);
				String contents = doc.get("meta");
				results.add(gson.fromJson(contents, SpaceFileDto.class));
			}

			resultPage.page = currentPage;
			resultPage.pageSize = pageSize;
			resultPage.totalPages = totalPages;
			resultPage.totalSize = totalCount;
			resultPage.entries = results;
		} catch (IndexNotFoundException e) {

		} finally {
			if (reader != null)
				reader.close();
		}

		return gson().toJson(resultPage);

	}
}
