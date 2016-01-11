package cn.ac.iscas.oncecloudshare.service.extensions.preview;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileConverter {
	/**
	 * 支持的文件类型
	 * 
	 * @return
	 */
	String[] getSupports();

	/**
	 * 转换器的名称
	 * 
	 * @return
	 */
	String getName();

	// /**
	// * 进行转换
	// *
	// * @param input
	// * @param output
	// */
	// void convert(File input, File output) throws Exception;
	//
	// /**
	// * 进行转换
	// *
	// * @param input
	// * @param output
	// */
	// void convert(File input, OutputStream output) throws Exception;

	/**
	 * 进行转换
	 * 
	 * @param input
	 * @param output
	 */
	String convert(InputStream input, String inputExtension, OutputStream output) throws Exception;
}