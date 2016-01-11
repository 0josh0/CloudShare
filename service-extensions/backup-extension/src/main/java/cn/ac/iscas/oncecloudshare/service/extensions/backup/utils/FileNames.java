package cn.ac.iscas.oncecloudshare.service.extensions.backup.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileNames {
	public static String getBackFileName(Date date) {
		return "bak_" + getSuffix(date) + ".sql";
	}

	public static String getBackErrorFileName(Date date) {
		return "bak_" + getSuffix(date) + ".err";
	}

	public static String getRecoverErrorFileName(Date date) {
		return "rec_" + getSuffix(date) + ".err";
	}

	public static boolean isBackUpFile(String fileName) {
		return fileName.matches("bak_\\d{14}\\.sql");
	}

	public static boolean isBackUpErrorFile(String fileName) {
		return fileName.matches("bak_\\d{14}\\.err");
	}

	protected static String getSuffix(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(date);
	}
}
