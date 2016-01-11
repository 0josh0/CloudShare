package cn.ac.iscas.oncecloudshare.service.service.filemeta;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.dto.file.DownloadTicketDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.UploadTicketDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.filecontent.BatchDownloadExceedLimitException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFile;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFileVersion;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.DownloadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UploadPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.common.PrincipalService;
import cn.ac.iscas.oncecloudshare.service.service.common.TempFileStorageService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.io.ZipByteSource;
import cn.ac.iscas.oncecloudshare.service.utils.io.ZipByteSource.ZipEntry;
import cn.ac.iscas.oncecloudshare.service.utils.wrapper.IntWrapper;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

@Service
public class FileContentService {
	private static final Logger _logger = LoggerFactory.getLogger(FileContentService.class);
	
	private static final long BYTES_PER_KB=1024;
	private static final long BYTES_PER_MB=1024*1024;
	private static final long BYTES_PER_GB=1024*1024*1024;

	@Autowired
	RuntimeContext runtimeContext;
	
	@Autowired
	PrincipalService pService;
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	
	@Autowired
	TempFileStorageService tfsService;
	
	public int getBatchDownloadNumberLimit(){
		return configService.getConfigAsInteger(
				Configs.Keys.BATCH_DOWNLOAD_NUMBER_LIMIT,0);
	}
	
	public long getBatchDownloadSizeLimit(){
		return configService.getConfigAsLong(
				Configs.Keys.BATCH_DOWNLOAD_SIZE_LIMIT,0L);
	}
	
	/**
	 * 产生下载单个文件的ticket
	 * @param fileVersion
	 * @return
	 */
	public DownloadTicketDto generateDownloadTicket(GenericFileVersion<?> fileVersion){
		DownloadPrincipal principal=new DownloadPrincipal(fileVersion.getMd5(),
				fileVersion.getFile().getName());
		long expiresIn=calculateDownloadExpireTime(fileVersion.getSize());
		String ticket=pService.storePrincipal(principal,expiresIn,true);
		return new DownloadTicketDto(ticket,expiresIn);
	}
	
	public DownloadTicketDto generateDownloadFilesTicket(List<? extends GenericFile<?>> files) throws IOException {
		Preconditions.checkArgument(files.size() > 0);
		// 如果是单文件下载
		if (files.size() == 1 && !files.get(0).getIsDir()) {
			return generateDownloadTicket(files.get(0).getHeadVersion());
		}
		// 检查限制条件
		checkDownloadNumberLimit(files);
		long size = checkDownloadSizeLimit(files);
		// 压缩文件并缓存
		final ZipByteSource zipByteSource = new ZipByteSource();
		final IntWrapper startIndex = new IntWrapper();
		Function<GenericFile<?>, Object> recursive = new Function<GenericFile<?>, Object>() {
			protected String getPath(GenericFile<?> file) {
				String path = file.getPath();
				return path.substring(startIndex.get(), path.length() - file.getName().length());
			}

			public Object apply(GenericFile<?> file) {
				if (!FileStatus.HEALTHY.equals(file.getStatus())) {
					return null;
				}
				if (file.getIsDir()) {
					ZipEntry entry = new ZipEntry(ByteStreams.asByteSource(ArrayUtils.EMPTY_BYTE_ARRAY), file.getName(), getPath(file), true);
					zipByteSource.addEntry(entry);
					for (GenericFile<?> child : file.getChildren()) {
						apply(child);
					}
				} else {
					try {
						ByteSource source = runtimeContext.getFileStorageService().retrieveFileContent(file.getHeadVersion().getMd5());
						ZipEntry entry = new ZipEntry(source, file.getName(), getPath(file));
						zipByteSource.addEntry(entry);
					} catch (IOException e) {
						_logger.error(null, e);
					}
				}
				return null;
			}
		};
		for (GenericFile<?> file : files) {
			String path = file.getPath();
			startIndex.set(path.length() - file.getName().length());
			recursive.apply(file);
		}

		long expiresIn = calculateDownloadExpireTime(size);
		String key = tfsService.saveTempFile(zipByteSource, expiresIn);
		DownloadPrincipal principal = new DownloadPrincipal(key, "云享打包下载的文件.zip");
		String ticket = pService.storePrincipal(principal, expiresIn, false);

		return new DownloadTicketDto(ticket, expiresIn);
	}
	
	/**
	 * 产生多文件打包下载的ticket
	 * @param fvList 
	 * @return
	 * @throws IOException
	 */
	public DownloadTicketDto generateDownloadTicket(List<? extends GenericFileVersion<?>> fvList) throws IOException{
		Preconditions.checkArgument(fvList.size()>0);
		if(fvList.size()==1){
			return generateDownloadTicket(fvList.get(0));
		}
		//检查限制条件
		int numberLimit=getBatchDownloadNumberLimit();
		if(fvList.size()>numberLimit){
			throw new BatchDownloadExceedLimitException(
					"batch download number limit: "+numberLimit);
		}
		long sizeLimit=getBatchDownloadSizeLimit();
		long size=0;
		for(GenericFileVersion<?> fv:fvList){
			size+=fv.getSize();
		}
		if(size>sizeLimit){
			throw new BatchDownloadExceedLimitException(
					"batch download size limit: "+sizeLimit);
		}
		
		
		ZipByteSource zipByteSource=new ZipByteSource();
		for(GenericFileVersion<?> fv:fvList){
			ByteSource source=runtimeContext.getFileStorageService()
					.retrieveFileContent(fv.getMd5());
			ZipEntry entry=new ZipEntry(source,fv.getFile().getName());
			zipByteSource.addEntry(entry);
		}
		
		long expiresIn=calculateDownloadExpireTime(size);
		String key=tfsService.saveTempFile(zipByteSource,expiresIn);
		DownloadPrincipal principal=new DownloadPrincipal(key,"云享打包下载的文件.zip");
		String ticket=pService.storePrincipal(principal,expiresIn,false);
		
		return new DownloadTicketDto(ticket,expiresIn);
	}
	
	private long calculateDownloadExpireTime(long size){
		if(size<5*BYTES_PER_MB){
			// <5MB -> 1h
			return 1*DateUtils.MILLIS_PER_HOUR;
		}
		else if(size<200*BYTES_PER_MB){
			// <200MB -> 8h
			return 4*DateUtils.MILLIS_PER_HOUR;
		}
		else{
			// >200MB -> 1d
			return 24*DateUtils.MILLIS_PER_HOUR;
		}
	}
	
	public UploadTicketDto generateFileUploadTicket(GenericFile<?> parentFolder) {
		UploadPrincipal principal = UploadPrincipal.createForFileUpload(parentFolder);
		long expireTime = calculateUploadExpireTime();
		String ticket = pService.storePrincipal(principal, expireTime, true);
		UploadTicketDto dto = UploadTicketDto.createForFileUpload(parentFolder.getId(), ticket, expireTime);
		return dto;
	}
	
	public UploadTicketDto generateFileUploadTicket(GenericFile<?> parentFolder, Long uploaderId) {
		UploadPrincipal principal = UploadPrincipal.createForFileUpload(parentFolder);
		principal.setUploaderId(uploaderId);
		long expireTime = calculateUploadExpireTime();
		String ticket = pService.storePrincipal(principal, expireTime, true);
		UploadTicketDto dto = UploadTicketDto.createForFileUpload(parentFolder.getId(), ticket, expireTime);
		return dto;
	}
	
	public UploadTicketDto generateFileVersionUploadTicket(GenericFile<?> file) {
		UploadPrincipal principal = UploadPrincipal.createForFileVersionUpload(file);
		long expireTime = calculateUploadExpireTime();
		String ticket = pService.storePrincipal(principal, expireTime, true);
		UploadTicketDto dto = UploadTicketDto.createForFileVersionUpload(file.getId(), ticket, expireTime);
		return dto;
	}
	
	/**
	 * 生成上传新版本的ticket
	 *
	 * @param file 文件
	 * @param uploaderId 上传者的id
	 * @return
	 */
	public UploadTicketDto generateFileVersionUploadTicket(GenericFile<?> file, Long uploaderId) {
		UploadPrincipal principal = UploadPrincipal.createForFileVersionUpload(file);
		principal.setUploaderId(uploaderId);
		long expireTime = calculateUploadExpireTime();
		String ticket = pService.storePrincipal(principal, expireTime, true);
		UploadTicketDto dto = UploadTicketDto.createForFileVersionUpload(file.getId(), ticket, expireTime);
		return dto;
	}
	
	/**
	 * 检查下载文件数的限制 
	 *
	 * @param files
	 * @return 文件的总个数
	 */
	protected int checkDownloadNumberLimit(List<? extends GenericFile<?>> files) {
		final int numberLimit = getBatchDownloadNumberLimit();
		if (files.size() > numberLimit) {
			throw new BatchDownloadExceedLimitException("batch download number limit: " + numberLimit);
		}
		final IntWrapper number = new IntWrapper();
		Function<GenericFile<?>, Object> recursive = new Function<GenericFile<?>, Object>() {
			public Object apply(GenericFile<?> file) {
				if (!FileStatus.HEALTHY.equals(file.getStatus())) {
					return null;
				}
				if (file.getIsDir()) {
					for (GenericFile<?> child : file.getChildren()) {
						apply(child);
					}
				} else {
					if (number.add(1) > numberLimit) {
						throw new BatchDownloadExceedLimitException("batch download number limit: " + numberLimit);
					}
				}
				return null;
			}
		};
		for (GenericFile<?> file : files) {
			recursive.apply(file);
		}
		return number.get();
	}
	
	/**
	 * 检查下载大小的限制
	 *
	 * @param files
	 * @return 文件的总大小
	 */
	protected long checkDownloadSizeLimit(List<? extends GenericFile<?>> files) {
		final long sizeLimit = getBatchDownloadSizeLimit();
		final AtomicLong size = new AtomicLong();
		Function<GenericFile<?>, Object> recursive = new Function<GenericFile<?>, Object>() {
			public Object apply(GenericFile<?> file) {
				if (!FileStatus.HEALTHY.equals(file.getStatus())) {
					return null;
				}
				if (file.getIsDir()) {
					for (GenericFile<?> child : file.getChildren()) {
						apply(child);
					}
				} else {
					if (size.addAndGet(file.getHeadVersion().getSize()) > sizeLimit) {
						throw new BatchDownloadExceedLimitException("batch download size limit: " + sizeLimit);
					}
				}
				return null;
			}
		};
		for (GenericFile<?> file : files) {
			recursive.apply(file);
		}
		return size.get();
	}
	
	private long calculateUploadExpireTime(){
		// 1 min
		return 5*DateUtils.MILLIS_PER_MINUTE;
	}
}
