package cn.ac.iscas.oncecloudshare.service.extensions.backup;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.extensions.backup.service.BackupService;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;

import com.google.common.collect.Sets;

@Component
public class BackupExtension implements ListenerExtension {
	@Resource
	private BackupService backupService;
	@Resource
	private TaskScheduler scheduler;
	
	public BackupExtension() {
	}

	@Override
	public Set<Object> getListeners() {
		return Sets.newHashSet();
	}
}