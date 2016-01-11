package cn.ac.iscas.oncecloudshare.service.tools;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlException;

public class WordReader {
	@SuppressWarnings("resource")
	public static String getWordText2007(InputStream ins) throws IOException, XmlException, OpenXML4JException {
		return new XWPFWordExtractor(OPCPackage.open(ins)).getText().trim();
	}

	@SuppressWarnings("resource")
	public static String getWordText2003(InputStream ins) throws IOException, XmlException, OpenXML4JException {
		return new WordExtractor(ins).getText().trim();
	}

}
