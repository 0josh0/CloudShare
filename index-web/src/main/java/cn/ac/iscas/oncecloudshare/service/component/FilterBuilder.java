package cn.ac.iscas.oncecloudshare.service.component;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.Filter;
import org.springframework.stereotype.Component;

@Component
public class FilterBuilder implements cn.ac.iscas.oncecloudshare.service.core.Filter {

	/**
	 * 构建过滤器，实现权限管理
	 * 
	 * @return 链式过滤器
	 */
	public ChainedFilter createrFilter(String values[]) {
		TermsFilter ownerFilter = new TermsFilter();

		for (int i = 0; i < values.length; i++) {
			ownerFilter.addTerm(new Term("owner", values[i]));
		}
		ChainedFilter filter = new ChainedFilter(new Filter[] { ownerFilter });
		return filter;
	}

}
