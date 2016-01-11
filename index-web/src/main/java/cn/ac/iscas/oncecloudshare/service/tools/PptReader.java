package cn.ac.iscas.oncecloudshare.service.tools;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.xmlbeans.XmlException;

public class PptReader {
	@SuppressWarnings("resource")
	public static String getPttText2003(InputStream ins) throws IOException {
		return new PowerPointExtractor(ins).getText().trim();

	}

	@SuppressWarnings("resource")
	public static String getPttText2007(InputStream ins) throws IOException, XmlException, OpenXML4JException {
		return new XSLFPowerPointExtractor(OPCPackage.open(ins)).getText().trim();
	}

}
