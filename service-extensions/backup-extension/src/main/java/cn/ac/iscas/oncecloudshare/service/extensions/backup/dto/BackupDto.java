package cn.ac.iscas.oncecloudshare.service.extensions.backup.dto;

import cn.ac.iscas.oncecloudshare.service.extensions.backup.model.Backup;

import com.google.common.base.Function;

public class BackupDto {
	public Long id;
	public String filePath;
	public String fileName;
	public Long createTime;

	public static final Function<Backup, BackupDto> adminTransformer = new Function<Backup, BackupDto>() {
		public BackupDto apply(Backup input) {
			if (input == null) {
				return null;
			}
			BackupDto output = new BackupDto();
			output.id = input.getId();
			output.filePath = input.getFilePath();
			output.fileName = input.getFileName();
			if (input.getCreateTime() != null) {
				output.createTime = input.getCreateTime().getTime();
			}
			return output;
		}
	};
}