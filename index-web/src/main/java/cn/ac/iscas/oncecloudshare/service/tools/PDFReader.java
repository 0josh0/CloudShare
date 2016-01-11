package cn.ac.iscas.oncecloudshare.service.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFReader {
	private static PDFTextStripper pdfStripper;
	static {
		try {
			pdfStripper = new PDFTextStripper();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getPDFtext(File file) throws IOException {
		PDDocument doc = PDDocument.load(file);
		String text = pdfStripper.getText(doc);// 获取文本
		doc.close();
		return text.trim();
	}
	
	public static String getPDFtext(InputStream ins) throws IOException {
		PDDocument doc = PDDocument.load(ins);
		String text = pdfStripper.getText(doc);// 获取文本
		doc.close();
		return text.trim();
	}
}
