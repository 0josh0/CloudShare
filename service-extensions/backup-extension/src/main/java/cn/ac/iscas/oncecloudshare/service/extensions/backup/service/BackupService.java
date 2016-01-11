package cn.ac.iscas.oncecloudshare.service.extensions.backup.service;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.dao.BackupDao;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.model.Backup;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.model.SystemInfo;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.utils.Configs;
import cn.ac.iscas.oncecloudshare.service.extensions.backup.utils.FileNames;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.io.Files;

@Service
@Transactional(readOnly = true)
public class BackupService {
	private static final Logger _logger = LoggerFactory.getLogger(BackupService.class);

	@Resource
	private BackupDao backupDao;
	@Resource(name="globalConfigService")
	private ConfigService configService;
	@Resource
	private TaskScheduler scheduler;

	public BackupService() {
	}

	@PostConstruct
	public void init() {
		// 用户备份
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				backupDB();
			}
		}, new CronTrigger(Configs.getUserExpr()));
		// 系统备份
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				File backFile = null;
				File errorFile = null;
				File backFolder = null;
				Date now = new Date();
				String backFileName = FileNames.getBackFileName(now);
				String errorFileName = FileNames.getBackErrorFileName(now);
				String sysPath = Configs.getSysPath();
				for (File parent : File.listRoots()) {
					backFolder = new File(parent.getAbsolutePath() + sysPath);
					try {
						backFolder.mkdirs();
					} catch (Exception e) {
						_logger.warn("忽略" + backFile.getAbsolutePath());
						continue;
					}
					boolean success = false;
					if (backFile != null) {
						try {
							Files.copy(backFile, new File(backFolder, backFileName));
							Files.copy(errorFile, new File(backFolder, errorFileName));
							success = true;
						} catch (Exception e) {
						}
					} else {
						try {
							backFile = new File(backFolder, backFileName);
							errorFile = new File(backFolder, errorFileName);
							getDatabaseBin().doDump(backFile, errorFile);
							success = true;
						} catch (Exception e) {
							backFile = null;
							errorFile = null;
						}
					}
					// 如果成功的话，删除原来的文件
					if (success) {
						for (File file : backFolder.listFiles()) {
							try {
								String name = file.getName();
								if (!name.equals(backFileName) && !name.equals(errorFileName)
										& (FileNames.isBackUpFile(name) || FileNames.isBackUpErrorFile(name))) {
									file.delete();
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}
		}, new CronTrigger(Configs.getSysExpr()));
	}

	/**
	 * 获取系统信息
	 * 
	 * @return
	 */
	public SystemInfo getDatabaseInfo() {
		SystemInfo sysInfo = new SystemInfo();
		sysInfo.setOsName(System.getProperty("os.name"));
		sysInfo.setDbPath(Configs.getMysqlBin());
		sysInfo.setBackupPath(Configs.getUserPath());
		return sysInfo;
	}

	public Backup findOne(long id) {
		return backupDao.findOne(id);
	}

	/**
	 * 通过过滤条件和分页参数查询备份
	 * 
	 * @param filters
	 * @param page
	 * @return
	 */
	public Page<Backup> findAll(List<SearchFilter> filters, Pageable page) {
		try {
			return backupDao.findAll(Specifications.fromFilters(filters, Backup.class), page);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 备份数据库
	 * 
	 * @param DBPath
	 * @param destPath
	 * @return
	 */
	@Transactional(readOnly = false)
	public Backup backupDB() {
		Date now = new Date();
		File bakFile = new File(Configs.getUserPath(), FileNames.getBackFileName(now));
		File errFile = new File(Configs.getUserPath(), FileNames.getBackErrorFileName(now));
		bakFile.getParentFile().mkdirs();
		if (!getDatabaseBin().doDump(bakFile, errFile)) {
			return null;
		}
		// 存储备份记录
		Backup backup = new Backup();
		backup.setFilePath(bakFile.getParent());
		backup.setFileName(bakFile.getName());
		backup = backupDao.save(backup);

		return backup;
	}

	/**
	 * 恢复数据库
	 * 
	 * @param backup
	 * @return
	 */
	public Backup recover(Backup backup) {
		File bakFile = new File(backup.getFilePath(), backup.getFileName());
		File errFile = new File(Configs.getUserPath(), FileNames.getRecoverErrorFileName(new Date()));
		if (!getDatabaseBin().doRecover(bakFile, errFile)) {
			return null;
		}
		return backup;
	}

	/**
	 * 从配置文件中读取数据库参数
	 * 
	 * @return
	 */
	protected DatabaseBin getDatabaseBin() {
		MysqlBin bin = new MysqlBin();
		bin.setSocket(Configs.getMysqlSocket());
		bin.setDbPath(Configs.getMysqlBin());
		bin.setPassword(Configs.getDbPassword());
		bin.setUserName(Configs.getDbUsername());
		String url = Configs.getDbUrl();
		bin.setDatabaseName(url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('?')));
		return bin;
	}
}