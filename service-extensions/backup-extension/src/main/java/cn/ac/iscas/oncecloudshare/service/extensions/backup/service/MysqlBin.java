package cn.ac.iscas.oncecloudshare.service.extensions.backup.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class MysqlBin implements DatabaseBin {
	private static final Logger _logger = LoggerFactory.getLogger(MysqlBin.class);

	public static final String FORMAT_DUMP = "%s/mysqldump -u%s -p%s %s %s %s";
	public static final String FORMAT_RECOVER = "%s/mysql -u%s -p%s %s %s";

	private String dbPath;
	private String userName;
	private String password;
	private String databaseName;
	private String tableName;
	private String socket;

	public boolean doDump(File bakFile, File errorFile) {
		synchronized (MysqlBin.class) {
			boolean result = false;
			try {
				String socket = StringUtils.isEmpty(getSocket()) ? StringUtils.EMPTY : ("--socket=" + getSocket());
				String command = String.format(FORMAT_DUMP, getDbPath(), getUserName(), getPassword(), socket, getDatabaseName(),
						StringUtils.isEmpty(getTableName()) ? StringUtils.EMPTY : getTableName());
				Process process = Runtime.getRuntime().exec(command);
				writeStreamToFile(process.getInputStream(), bakFile);
				writeStreamToFile(process.getErrorStream(), errorFile);
				process.waitFor();
				if (process.exitValue() == 0) {
					result = true;
				}
			} catch (Exception e) {
				_logger.error(null, e);
			}
			return result;
		}
	}

	public boolean doRecover(File bakFile, File errorFile) {
		synchronized (MysqlBin.class) {
			String socket = StringUtils.isEmpty(getSocket()) ? StringUtils.EMPTY : ("--socket=" + getSocket());
			String command = String.format(FORMAT_RECOVER, getDbPath(), getUserName(), getPassword(), socket, getDatabaseName(),
					StringUtils.isEmpty(getTableName()) ? StringUtils.EMPTY : getTableName());
			try {
				Process process = Runtime.getRuntime().exec(command);
				writeStreamToFile(process.getErrorStream(), errorFile);
				Files.copy(bakFile, process.getOutputStream());
				return true;
			} catch (IOException e) {
				_logger.error(null, e);
			}
			return false;
		}
	}

	private void writeStreamToFile(final InputStream in, final File file) {
		new Thread() {
			public void run() {
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					IOUtils.copy(in, out);
				} catch (Exception e) {
					_logger.error(null, e);
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
		}.start();
	}

	// ================= getters and setters ==================

	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSocket() {
		return socket;
	}

	public void setSocket(String socket) {
		this.socket = socket;
	}
}