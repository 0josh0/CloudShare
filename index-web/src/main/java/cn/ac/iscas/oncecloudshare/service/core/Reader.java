package cn.ac.iscas.oncecloudshare.service.core;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;


public interface Reader {

	public IndexReader createReader( ) throws IOException;
}
