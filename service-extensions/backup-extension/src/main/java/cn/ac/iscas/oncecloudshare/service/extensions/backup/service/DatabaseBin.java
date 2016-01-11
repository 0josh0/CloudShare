package cn.ac.iscas.oncecloudshare.service.extensions.backup.service;

import java.io.File;

public interface DatabaseBin {
	boolean doDump(File bakFile, File errorFile);
	
	boolean doRecover(File bakFile, File errorFile);
}
