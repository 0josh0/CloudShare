package cn.ac.iscas.oncecloudshare.service.service.filemeta;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.filemeta.FileVersionDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.DeleteLastVersionException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.DuplicatePathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InsufficientQuotaException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidFramentIdException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.Tag;
import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.http.MimeTypes;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

@Service
@Transactional(rollbackFor = Throwable.class)
public class FileService extends BaseFileService {

	@Autowired
	FolderService folderService;

	@Autowired
	private FileVersionDao fvDao;
	@Resource
	private TagService tagService;

	LockSet<Long> uploadLockSet = new LockSet<Long>();

	LockSet<Long> fileLockSet = new LockSet<Long>();

	/**
	 * 查找普通文件（非文件夹）
	 * 
	 * @return
	 */
	public File findFile(long fileId) {
		File file = find(fileId);
		if (file == null || file.getIsDir()) {
			return null;
		}
		return file;
	}

	/**
	 * 查找文件版本
	 * 
	 * @param fileId
	 * @param version
	 * @return
	 */
	public FileVersion findFileVersion(long fileId, int version) {
		FileVersion fv = fvDao.findByFileAndVersion(fileId, version);
		if (fv == null) {
			return null;
		} else {
			return fv.getFile().getStatus() == FileStatus.DELETED ? null : fv;
		}
	}

	/**
	 * 查找文件版本
	 * 
	 * @param fileId
	 * @param version
	 * @return
	 */
	public FileVersion findFileVersion(long fvId) {
		FileVersion fv = fvDao.findOne(fvId);
		if (fv == null) {
			return null;
		} else {
			return fv.getFile().getStatus() == FileStatus.DELETED ? null : fv;
		}
	}

	public List<FileVersion> findFileVersionsByOwner(long ownerId, List<Long> fileVersionIdList) {
		return fvDao.findFileVersionsByOwner(ownerId, fileVersionIdList);
	}

	/**
	 * 
	 * @param fileId
	 * @return
	 * @throws IllegalArgumentException 文件不存在
	 */
	public File findExistingFile(long fileId) {
		return findExisting(fileId, false);
	}

	/**
	 * 获取文件内容
	 * 
	 * @param fv
	 * @return
	 * @throws IOException
	 */
	public ByteSource getFileContent(FileVersion fv) throws IOException {
		return runtimeContext.getFileStorageService().retrieveFileContent(fv.getMd5());
	}

	private FileSource saveFileContentInternal(User owner, String md5, ByteSource fileContent) throws IOException {
		FileStorageService fsService = runtimeContext.getFileStorageService();

		if (md5 != null) {
			FileSource fileSource = null;
			if ((fileSource = fsService.findFileSource(md5)) != null) {
				uService.incrRestQuota(owner, -fileSource.getSize());
				return fileSource;
			}
		}
		if (fileContent != null) {
			uService.incrRestQuota(owner, -fileContent.size());
			return fsService.saveFile(fileContent);
		}
		throw new IOException("error saving file");
	}

	private String findAvailablePath(long ownerId, String parentPath, String filename) {
		String path = concatPath(parentPath, filename);
		if (findHealthyByPath(ownerId, path) == null) {
			return path;
		}
		String name = Files.getNameWithoutExtension(filename);
		String ext = Files.getFileExtension(filename);
		for (int i = 0;;) {
			String newName = name + "(" + (++i) + ")";
			if (!Strings.isNullOrEmpty(ext)) {
				newName = newName + "." + ext;
			}
			path = concatPath(parentPath, newName);
			if (findHealthyByPath(ownerId, path) == null) {
				return path;
			}
		}
		// 直接返回DuplicatePathException
		// String path=concatPath(parentPath,filename);
		// if(findHealthyByPath(ownerId,path)!=null){
		// throw new DuplicatePathException(path);
		// }
	}

	/**
	 * 保存新文件，使用“md5或者fileContent”指定具体的物理文件
	 * 
	 * @param ownerId
	 * @param parentId
	 * @param name
	 * @param md5
	 * @param fileContent
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException ownerId或parentId无效
	 * @throws InsufficientQuotaException 配额不足
	 * @throws DuplicatePathException 文件已存在
	 */
	public FileVersion saveNewFile(long ownerId, long parentId, String name, String md5, ByteSource fileContent) throws IOException,
			IllegalArgumentException, InsufficientQuotaException, DuplicatePathException {
		// 对parentId加锁，不允许同时往同一目录上传多个文件
		Lock lock = uploadLockSet.getLock(parentId);
		lock.lock();
		try {
			User owner = uService.findExistingUser(ownerId);
			File parent = folderService.findExistingFolder(parentId);

			String path = findAvailablePath(ownerId, parent.getPath(), name);

			// save file meta
			File file = new File();
			file.setIsDir(false);
			file.setParent(parent);
			file.setPath(path);
			file.setStatus(FileStatus.HEALTHY);
			file.setOwner(owner);
			file.setVersionSeq(1);
			file.setMimeType(MimeTypes.lookupMimeTypeByFilename(name));
			fDao.save(file);

			// save file content
			FileSource source = saveFileContentInternal(owner, md5, fileContent);

			// save file version
			FileVersion fileVersion = new FileVersion();
			fileVersion.setFile(file);
			fileVersion.setVersion(0);
			fileVersion.setMd5(source.getMd5());
			fileVersion.setSize(source.getSize());
			fvDao.save(fileVersion);

			file.setVersions(ImmutableList.of(fileVersion));
			return fileVersion;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 保存新版本，使用“md5或者fileContent”指定具体的物理文件
	 * 
	 * @param fileId
	 * @param md5
	 * @param fileContent
	 * @return
	 * @throws IOException
	 * @throws InsufficientQuotaException
	 */
	public FileVersion saveNewFileVersion(long fileId, String md5, ByteSource fileContent) throws IOException, InsufficientQuotaException {
		Lock lock = fileLockSet.getLock(fileId);
		lock.lock();
		try {
			File file = findExistingFile(fileId);

			FileSource source = saveFileContentInternal(file.getOwner(), md5, fileContent);

			fDao.incrVersionSeq(fileId);

			FileVersion fileVersion = new FileVersion();
			fileVersion.setFile(file);
			fileVersion.setVersion(file.getVersionSeq());
			fileVersion.setMd5(source.getMd5());
			fileVersion.setSize(source.getSize());
			fvDao.save(fileVersion);

			return fileVersion;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 移动文件
	 * 
	 * @param fileId
	 * @param parentId
	 * @param name
	 * @throws IllegalArgumentException 文件状态为ANCESTOR_TRASHED
	 * @throws DuplicatePathException
	 */
	public void moveFile(long fileId, long parentId, String name) throws DuplicatePathException {

		File file = findExistingFile(fileId);
		File parent = folderService.findExistingFolder(parentId);

		String path = concatPath(parent.getPath(), name);
		checkMoveable(file, path, name);

		file.setParent(parent);
		file.setPath(path);
		fDao.save(file);
	}

	/**
	 * 移入回收站
	 * 
	 * @param fileId
	 */
	public void trash(long fileId) {
		checkTrashable(findExistingFile(fileId));
		fDao.updateStatus(fileId, FileStatus.TRASHED);
	}

	/**
	 * 从回收站还原
	 * 
	 * @param fileId
	 */
	public void untrash(long fileId) {
		File file = findExistingFile(fileId);
		checkUntrashable(file, file.getPath());
		fDao.updateStatus(fileId, FileStatus.HEALTHY);
	}

	/**
	 * 从回收站还原
	 * 
	 * @param fileId
	 */
	public void untrashTo(long fileId, long parentId, String name) {
		File file = findExistingFile(fileId);
		File parent = folderService.findExistingFolder(parentId);
		String path = concatPath(parent.getPath(), name);
		checkUntrashable(file, path);
		moveFile(fileId, parentId, name);
		fDao.updateStatus(fileId, FileStatus.HEALTHY);
	}

	/**
	 * 删除文件
	 * 
	 * @param fileId
	 */
	public void delete(long fileId) {
		File file = findExistingFile(fileId);
		checkDeletable(file);
		fDao.updateStatus(fileId, FileStatus.DELETED);
		removeTags(file, Lists.newArrayList(file.getTags()));
		uService.incrRestQuota(file.getOwner(), file.getTotalSize());
	}

	/**
	 * 删除文件版本（不能删除最后一个版本）
	 * 
	 * @param fileId
	 * @param version
	 */
	public void deleteVersion(long fileId, int version) {
		FileVersion fv = findFileVersion(fileId, version);
		if (fv != null) {
			if (fv.getFile().getVersions().size() == 1) {
				throw new DeleteLastVersionException();
			}
			uService.incrRestQuota(fv.getFile().getOwner(), fv.getSize());
			fvDao.delete(fv);
		}
	}

	/**
	 * 保存临时文件片段
	 * 
	 * @param ownerId
	 * @param fileContent
	 * @return
	 * @throws InsufficientQuotaException
	 * @throws DuplicatePathException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public FileVersion saveTempFileFragment(long ownerId, ByteSource fileContent) throws InsufficientQuotaException, DuplicatePathException,
			IllegalArgumentException, IOException {
		String filename = UUID.randomUUID().toString();
		return saveNewFile(ownerId, folderService.findTempDir(ownerId).getId(), filename, null, fileContent);
	}

	private FileVersion mergeTempFragments(long ownerId, List<Long> fragmentIdList, SaveTempFragmentsAction action) throws IOException {

		Iterable<File> files = Iterables.filter(fDao.findMultiFilesByOwner(ownerId, fragmentIdList), new Predicate<File>() {

			@Override
			public boolean apply(File input) {
				if (input.getIsDir()) {
					return false;
				}
				if (!input.getPath().startsWith(TEMP_PATH)) {
					return false;
				}
				return true;
			}
		});

		List<String> fragmentMd5List = Lists.newArrayList();
		for (File file : files) {
			fragmentMd5List.add(file.getHeadVersion().getMd5());
		}
		if (fragmentMd5List.size() != fragmentIdList.size()) {
			throw new InvalidFramentIdException();
		}

		FileVersion fv = action.apply(new TempFragmentsByteSource(fragmentMd5List));

		for (File file : files) {
			delete(file.getId());
		}

		return fv;
	}

	/**
	 * 合并临时文件片段，使之成为新文件
	 * 
	 * @param ownerId
	 * @param parentId
	 * @param name
	 * @param fragmentIdList 文件片段的id
	 * @return
	 * @throws InsufficientQuotaException
	 * @throws DuplicatePathException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public FileVersion mergeTempFragmentsAsNewFile(final long ownerId, List<Long> fragmentIdList, final long parentId, final String name)
			throws IOException {

		return mergeTempFragments(ownerId, fragmentIdList, new SaveTempFragmentsAction() {

			@Override
			public FileVersion apply(ByteSource source) throws IOException {
				return saveNewFile(ownerId, parentId, name, null, source);
			}
		});
	}

	/**
	 * 合并临时文件片段，使之成为新版本
	 * 
	 * @param ownerId
	 * @param fileId
	 * @param fragmentIdList
	 * @return
	 * @throws IOException
	 */
	public FileVersion mergeTempFragmentsAsNewFileVersion(final long ownerId, List<Long> fragmentIdList, final long fileId) throws IOException {
		return mergeTempFragments(ownerId, fragmentIdList, new SaveTempFragmentsAction() {

			@Override
			public FileVersion apply(ByteSource source) throws IOException {
				return saveNewFileVersion(fileId, null, source);
			}
		});
	}

	private static interface SaveTempFragmentsAction {

		FileVersion apply(ByteSource source) throws IOException;
	}

	/**
	 * 将多个临时文件片段组合成为ByteSource
	 * 
	 * @author Chen Hao
	 */
	private class TempFragmentsByteSource extends ByteSource {

		List<String> fragmentMd5List;

		public TempFragmentsByteSource(List<String> fragmentMd5List) {
			this.fragmentMd5List = fragmentMd5List;
		}

		@Override
		public InputStream openStream() throws IOException {
			return new SequenceInputStream(new Enumeration<InputStream>() {

				int index = 0;

				@Override
				public boolean hasMoreElements() {
					return index < fragmentMd5List.size();
				}

				@Override
				public InputStream nextElement() {
					index++;
					try {
						return runtimeContext.getFileStorageService().retrieveFileContent(fragmentMd5List.get(index - 1)).openStream();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}

	}

	public File saveNewFile(File parent, String name) {
		// save file meta
		File file = new File();
		file.setIsDir(false);
		file.setParent(parent);
		file.setPath(findAvailablePath(parent.getOwner().getId(), parent.getPath(), name));
		file.setStatus(FileStatus.HEALTHY);
		file.setOwner(parent.getOwner());
		file.setVersionSeq(1);
		file.setMimeType(MimeTypes.lookupMimeTypeByFilename(name));
		fDao.save(file);
		return file;
	}

	public void addTags(long fileId, long[] tags) {
		File file = findExisting(fileId, false);
		addTags(file, tags);
	}

	public void addTags(File file, long[] tags) {
		for (long tagId : tags) {
			Tag tag = tagService.findOne(tagId);
			if (tag != null) {
				if (file.addTag(tag)) {
					tagService.updateFilesCount(tagId, 1);
				}
			}
		}
		fDao.save(file);
	}

	public void removeTags(long fileId, long[] tags) {
		File file = findExisting(fileId, false);
		removeTags(file, tags);
	}

	public void removeTags(File file, long[] tags) {
		List<Tag> list = Lists.newArrayList();
		for (long tagId : tags) {
			Tag tag = tagService.findOne(tagId);
			if (tag != null) {
				list.add(tag);
			}
		}
		removeTags(file, list);
	}

	public void removeTags(File file, Collection<Tag> tags) {
		for (Tag tag : tags) {
			if (file.removeTag(tag)) {
				tagService.updateFilesCount(tag.getId(), -1);
			}
		}
		fDao.save(file);
	}
}
