package cn.ac.iscas.oncecloudshare.service.core;

import org.apache.lucene.queries.ChainedFilter;

public interface Filter {

	public ChainedFilter createrFilter(String values[]);
}
