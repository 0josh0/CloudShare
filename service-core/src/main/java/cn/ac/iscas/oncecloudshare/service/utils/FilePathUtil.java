package cn.ac.iscas.oncecloudshare.service.utils;

import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;


public class FilePathUtil {
	
	public static final String ROOT_PATH="/";
	
	public static final String VALID_FILE_NAME_REGEX="[^\\/\\\\:\\*\\?\\\"\\<\\>\\|]{1,64}";
	public static final Pattern VALID_FILE_NAME_PATTERN=Pattern.compile(VALID_FILE_NAME_REGEX);
	
	public static String normalizePath(String path){
		return FilenameUtils.normalizeNoEndSeparator(path,true);
	}
	
	public static String normalizeFilename(String filename){
		filename=filename.trim();
		if(VALID_FILE_NAME_PATTERN.matcher(filename).matches()){
			return filename;
		}
		else{
			return null;
		}
	}

	public static String extractFilenameFromPath(String path){
		path=normalizePath(path);
		if(path!=null){
			int index=path.lastIndexOf('/');
			return path.substring(index+1);
		}
		return null;
	}
	
	public static String concatPath(String basePath,String subFilename){
		basePath=normalizePath(basePath);
		subFilename=normalizeFilename(subFilename);
		if(basePath==null || subFilename==null){
			return null;
		}
		String path=FilenameUtils.concat(basePath,subFilename);
		if(path!=null){
			path=path.replace('\\','/');
		}
		return path;
	}
}
