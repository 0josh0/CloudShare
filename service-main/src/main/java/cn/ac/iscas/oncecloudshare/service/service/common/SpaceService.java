package cn.ac.iscas.oncecloudshare.service.service.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceDao;
import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceFileDao;
import cn.ac.iscas.oncecloudshare.service.dao.common.SpaceFileVersionDao;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.DuplicatePathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.FileUnmodifiableException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InsufficientQuotaException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidDestinationPathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidFramentIdException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidPathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.Md5FileNotFoundException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileVersion;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceTag;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;
import cn.ac.iscas.oncecloudshare.service.service.filestorage.FileStorageService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.concurrent.LockSet;
import cn.ac.iscas.oncecloudshare.service.utils.http.MimeTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

@Service
@Transactional(readOnly = true)
public class SpaceService {
	private static final Logger _logger = LoggerFactory.getLogger(SpaceService.class);

	@Resource
	private SpaceDao spaceDao;
	@Resource
	private RuntimeContext runtimeContext;
	@Resource
	private SpaceFileDao spaceFileDao;

	@Resource
	private SpaceFileVersionDao spaceFileVersionDao;
	@Resource(name = "globalConfigService")
	private ConfigService<?> configService;
	@Resource
	private SpaceTagService spaceTagService;

	private LockSet<Long> uploadLockSet = new LockSet<Long>();
	private LockSet<Long> fileLockSet = new LockSet<Long>();
	// 对空间进行更新的锁
	private LockSet<Long> spaceLocks = new LockSet<Long>();

	/**
	 * 为空间创建内置文件夹（如：根目录，临时目录，备份目录）
	 * 
	 * @param space 对应的空间
	 */
	@Transactional(readOnly = false)
	public void makeBuildinFolders(BaseSpace space) {
		for (String path : Constants.BuildInFolders.ALL) {
			SpaceFile file = new SpaceFile();
			file.setModifiable(false);
			file.setIsDir(true);
			file.setParent(null);
			file.setPath(path);
			file.setStatus(FileStatus.HEALTHY);
			file.setOwner(space);
			file.setVersionSeq(0);
			spaceFileDao.save(file);
		}
	}

	/**
	 * 根目录
	 * 
	 * @param ownerId
	 * @return
	 */
	public SpaceFile findRoot(long spaceId) {
		return findByPath(spaceId, Constants.BuildInFolders.ROOT);
	}

	/**
	 * 临时目录
	 * 
	 * @param ownerId
	 * @return
	 */
	public SpaceFile findTempDir(long spaceId) {
		return findByPath(spaceId, Constants.BuildInFolders.TEMP);
	}

	/**
	 * 备份目录
	 * 
	 * @param ownerId
	 * @return
	 */
	public SpaceFile findBackupDir(long ownerId) {
		return findByPath(ownerId, Constants.BuildInFolders.BACKUP);
	}

	public SpaceFile findByPath(long ownerId, String path) {
		path = FilePathUtil.normalizePath(path);
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, ownerId));
		filters.add(new SearchFilter("path", Operator.EQ, path));
		filters.add(new SearchFilter("status", Operator.NE, FileStatus.DELETED));
		Iterator<SpaceFile> files = spaceFileDao.findAll(Specifications.fromFilters(filters, SpaceFile.class)).iterator();
		SpaceFile file = null;
		if (files.hasNext()) {
			file = files.next();
		}
		if (files.hasNext()) {
			_logger.warn("工作空间#{}中路径#{}对应有多个文件", ownerId, path);
		}
		return file;
	}

	/**
	 * 处理重名的冲突
	 * 
	 * @param parent
	 * @param name
	 * @return
	 */
	protected String findAvailablePath(long ownerId, String parentPath, String filename) {
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

	public SpaceFile findHealthyByPath(long ownerId, String path) {
		path = FilePathUtil.normalizePath(path);
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, ownerId));
		filters.add(new SearchFilter("path", Operator.EQ, path));
		filters.add(new SearchFilter("status", Operator.EQ, FileStatus.HEALTHY));
		return spaceFileDao.findOne(Specifications.fromFilters(filters, SpaceFile.class));
	}

	public SpaceFile findFile(long fileId) {
		SpaceFile file = spaceFileDao.findOne(fileId);
		if (file.getStatus() == FileStatus.DELETED) {
			return null;
		}
		return file;
	}

	/**
	 * 返回healthy的children
	 * 
	 * @param parentId
	 * @param isDir true:只列出文件夹，false：只列出文件，null：列出文件和文件夹
	 * @param pageable
	 * @return
	 */
	public Page<SpaceFile> findChildren(long parentId, Boolean isDir, Pageable pageable) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("parent.id", Operator.EQ, parentId));
		filters.add(new SearchFilter("status", Operator.EQ, "HEALTHY"));
		filters.add(new SearchFilter("modifiable", Operator.EQ, Boolean.TRUE));
		if (isDir != null) {
			filters.add(new SearchFilter("isDir", Operator.EQ, isDir));
		}
		return spaceFileDao.findAll(Specifications.fromFilters(filters, SpaceFile.class), pageable);
	}

	/**
	 * 新建文件夹
	 * 
	 * @param ownerId
	 * @param parentId
	 * @param name
	 * @return
	 * @throws DuplicatePathException
	 */
	@Transactional(readOnly = false)
	public SpaceFile makeFolder(User creator, BaseSpace space, SpaceFile parent, String name) throws InvalidPathException, DuplicatePathException {
		String path = concatPath(parent.getPath(), name);
		if (findHealthyByPath(space.getId(), path) != null) {
			throw new DuplicatePathException(path);
		}
		// save file meta
		SpaceFile file = new SpaceFile();
		file.setIsDir(true);
		file.setParent(parent);
		file.setPath(path);
		file.setStatus(FileStatus.HEALTHY);
		file.setOwner(space);
		file.setVersionSeq(0);
		file.setCreator(creator);
		spaceFileDao.save(file);

		return file;
	}

	/**
	 * 查找某个文件夹
	 * 
	 * @param folderId
	 * @return
	 */
	public SpaceFile findFolder(long folderId) {
		SpaceFile folder = spaceFileDao.findOne(folderId);
		return folder != null && folder.getIsDir() ? folder : null;
	}

	/**
	 * 查找工作空间中的某个文件夹
	 * 
	 * @param workspace
	 * @param folderId
	 * @return
	 */
	public SpaceFile findFolder(BaseSpace space, long folderId) {
		SpaceFile folder = findFolder(folderId);
		if (folder == null || !space.hasFile(folder)) {
			return null;
		}
		return folder;
	}

	@Transactional(readOnly = false)
	public void move(SpaceFile toRemove, SpaceFile newParent, String name) {
		final int oldPathLength = toRemove.getPath().length();
		final String newPath = concatPath(newParent.getPath(), name);

		checkMoveable(toRemove, newPath, name);

		toRemove.setParent(newParent);
		toRemove.setPath(newPath);
		spaceFileDao.save(toRemove);
		/*
		 * 递归修改子文件的path
		 */
		if (toRemove.getIsDir()) {
			executeRecursively(toRemove, new RecursiveAction() {
				@Override
				public void apply(SpaceFile file) {
					file.setPath(newPath + file.getPath().substring(oldPathLength));
					spaceFileDao.save(file);
				}
			});
		}
	}

	/**
	 * 将文件放入垃圾箱
	 * 
	 * @param file
	 */
	@Transactional(readOnly = false)
	public void trash(SpaceFile file) {
		checkTrashable(file);
		setStatusRecursively(file, FileStatus.TRASHED, FileStatus.ANCESTOR_TRASHED);
	}

	/**
	 * 还原文件
	 * 
	 * @param spaceFile
	 */
	@Transactional(readOnly = false)
	public void untrash(SpaceFile spaceFile) {
		checkUntrashable(spaceFile, spaceFile.getPath());
		setStatusRecursively(spaceFile, FileStatus.HEALTHY, FileStatus.HEALTHY);
	}

	/**
	 * 还原文件到指定目录
	 * 
	 * @param spaceFile
	 * @param parent
	 * @param name
	 */
	@Transactional(readOnly = false)
	public void untrashTo(SpaceFile spaceFile, SpaceFile parent, String name) {
		String path = concatPath(parent.getPath(), name);
		checkUntrashable(spaceFile, path);
		move(spaceFile, parent, name);
		setStatusRecursively(spaceFile, FileStatus.HEALTHY, FileStatus.HEALTHY);
	}

	/**
	 * 更新文件的信息
	 * 
	 * @param file
	 * @param description
	 */
	@Transactional(readOnly = false)
	public void updateInfo(SpaceFile file, String description) {
		if (description != null) {
			file.setDescription(description);
		}
		spaceFileDao.save(file);
	}

	/**
	 * 删除文件
	 * 
	 * @param toDelete
	 */
	@Transactional(readOnly = false)
	public void delete(SpaceFile toDelete) {
		checkDeletable(toDelete);

		toDelete.setStatus(FileStatus.DELETED);
		// 删除的时候清除tag
		removeTags(toDelete, Lists.newArrayList(toDelete.getTags()));
		spaceFileDao.save(toDelete);

		class LongWrapper {
			long value = 0;
		}
		final LongWrapper totalSize = new LongWrapper();
		if (toDelete.getIsDir()) {
			executeRecursively(toDelete, new RecursiveAction() {
				@Override
				public void apply(SpaceFile file) {
					if (file.getStatus() != FileStatus.DELETED) {
						file.setStatus(FileStatus.DELETED);
						spaceFileDao.save(file);
						totalSize.value = totalSize.value + file.getTotalSize();
					}
				}
			});
		} else {
			totalSize.value = toDelete.getTotalSize();
		}
		incrRestQuota(toDelete.getOwner(), totalSize.value);
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
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion saveNewFile(User creator, SpaceFile parent, String name, String md5, ByteSource fileContent) throws IOException,
			IllegalArgumentException, InsufficientQuotaException, DuplicatePathException {
		// 对parentId加锁，不允许同时往同一目录上传多个文件
		Lock lock = uploadLockSet.getLock(parent.getId());
		lock.lock();
		try {
			String path = findAvailablePath(parent.getOwner().getId(), parent.getPath(), name);
			// save file meta
			SpaceFile file = new SpaceFile();
			file.setIsDir(false);
			file.setParent(parent);
			file.setPath(path);
			file.setStatus(FileStatus.HEALTHY);
			file.setOwner(parent.getOwner());
			file.setVersionSeq(1);
			file.setMimeType(MimeTypes.lookupMimeTypeByFilename(name));
			file.setCreator(creator);
			spaceFileDao.save(file);

			// save file content
			FileSource source = saveFileContentInternal(parent.getOwner(), md5, fileContent);

			// save file version
			SpaceFileVersion fileVersion = new SpaceFileVersion();
			fileVersion.setFile(file);
			fileVersion.setVersion(0);
			fileVersion.setMd5(source.getMd5());
			fileVersion.setSize(source.getSize());
			fileVersion.setCreator(creator);
			spaceFileVersionDao.save(fileVersion);

			return fileVersion;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 保存临时文件
	 * 
	 * @param creator
	 * @param space
	 * @param fileName
	 * @param fileContent
	 * @return
	 * @throws InsufficientQuotaException
	 * @throws DuplicatePathException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion saveTempFile(User creator, BaseSpace space, String fileName, ByteSource fileContent) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		if (Strings.isNullOrEmpty(fileName)) {
			fileName = UUID.randomUUID().toString().replace("-", "");
		}
		return saveNewFile(creator, findTempDir(space.getId()), fileName, null, fileContent);
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion saveTempFile(User creator, BaseSpace space, String fileName, String md5) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		if (Strings.isNullOrEmpty(fileName)) {
			fileName = UUID.randomUUID().toString().replace("-", "");
		}
		return saveNewFile(creator, findTempDir(space.getId()), fileName, md5, null);
	}

	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion saveTempFileFragment(User creator, BaseSpace space, ByteSource fileContent) throws InsufficientQuotaException,
			DuplicatePathException, IllegalArgumentException, IOException {
		String filename = UUID.randomUUID().toString();
		return saveNewFile(creator, findTempDir(space.getId()), filename, null, fileContent);
	}

	/**
	 * 合并临时文件片段
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
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion mergeTempFileFragment(User creator, SpaceFile parent, String name, List<Long> fragmentIdList)
			throws InsufficientQuotaException, DuplicatePathException, IllegalArgumentException, IOException {
		Iterable<SpaceFile> files = Iterables.filter(spaceFileDao.findMultiFilesByOwner(parent.getOwner().getId(), fragmentIdList),
				new Predicate<SpaceFile>() {
					@Override
					public boolean apply(SpaceFile input) {
						if (input.getIsDir()) {
							return false;
						}
						if (!input.getPath().startsWith(Constants.BuildInFolders.TEMP)) {
							return false;
						}
						return true;
					}
				});

		List<String> fragmentMd5List = Lists.newArrayList();
		for (SpaceFile file : files) {
			fragmentMd5List.add(file.getHeadVersion().getMd5());
		}
		if (fragmentMd5List.size() != fragmentIdList.size()) {
			throw new InvalidFramentIdException();
		}

		SpaceFileVersion fv = saveNewFile(creator, parent, name, null, new FileFragmentsByteSource(fragmentMd5List));

		for (SpaceFile file : files) {
			delete(file);
		}

		return fv;
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
	@Transactional(readOnly = false, rollbackFor = Throwable.class)
	public SpaceFileVersion saveNewFileVersion(User creator, SpaceFile file, String md5, ByteSource fileContent) throws IOException,
			InsufficientQuotaException {
		Lock lock = fileLockSet.getLock(file.getId());
		lock.lock();
		try {
			FileSource source = saveFileContentInternal(file.getOwner(), md5, fileContent);
			spaceFileDao.incrVersionSeq(file.getId());

			SpaceFileVersion fileVersion = new SpaceFileVersion();
			fileVersion.setFile(file);
			fileVersion.setVersion(file.getVersionSeq());
			fileVersion.setMd5(source.getMd5());
			fileVersion.setSize(source.getSize());
			fileVersion.setCreator(creator);
			spaceFileVersionDao.save(fileVersion);

			return fileVersion;
		} finally {
			lock.unlock();
		}
	}

	private FileSource saveFileContentInternal(BaseSpace space, String md5, ByteSource fileContent) throws IOException {
		FileStorageService fsService = runtimeContext.getFileStorageService();
		if (md5 != null) {
			FileSource fileSource = null;
			if ((fileSource = fsService.findFileSource(md5)) != null) {
				incrRestQuota(space, -fileSource.getSize());
				return fileSource;
			}
		}
		if (fileContent != null) {
			incrRestQuota(space, -fileContent.size());
			return fsService.saveFile(fileContent);
		}
		throw new IOException("error saving file");
	}

	/**
	 * 增加restQuota
	 * 
	 * @param space 要更新的空间
	 * @param increment 增加量（可以为负数）
	 * @throws InsufficientQuotaException 配额不足
	 */
	@Transactional(readOnly = false)
	public void incrRestQuota(BaseSpace space, long increment) throws InsufficientQuotaException {
		Lock lock = spaceLocks.getLock(space.getId());
		try {
			lock.lock();
			if (space.getRestQuota() + increment < 0) {
				throw new InsufficientQuotaException();
			}
			int res = spaceDao.incrRestQuota(space.getId(), increment);
			if (res == 0) {
				throw new InsufficientQuotaException();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 查询文件
	 * 
	 * @param filters
	 * @return
	 */
	public List<SpaceFile> findFiles(List<SearchFilter> filters) {
		try {
			Specification<SpaceFile> spec = Specifications.fromFilters(filters, SpaceFile.class);
			return spaceFileDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 搜索文件
	 * 
	 * @param filters
	 * @param pageable
	 * @return
	 */
	public Page<SpaceFile> findFiles(List<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<SpaceFile> spec = Specifications.fromFilters(filters, SpaceFile.class);
			return spaceFileDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	/**
	 * 递归修改文件状态
	 * 
	 * @param rootId 根目录id
	 * @param rootStatus 根目录状态
	 * @param childrenStatus 子文件状态
	 */
	private void setStatusRecursively(SpaceFile file, FileStatus rootStatus, final FileStatus childrenStatus) {
		file.setStatus(rootStatus);
		spaceFileDao.save(file);
		if (file.getIsDir()) {
			executeRecursively(file, new RecursiveAction() {
				@Override
				public void apply(SpaceFile file) {
					if (file.getStatus() != FileStatus.DELETED) {
						file.setStatus(childrenStatus);
						spaceFileDao.save(file);
					}
				}
			});
		}
	}

	/**
	 * 判断文件是否可以放入垃圾箱
	 * 
	 * @param file
	 */
	protected void checkTrashable(SpaceFile file) {
		checkModifiable(file);
		Preconditions.checkArgument(file.getStatus() == FileStatus.HEALTHY, "cannot trash file with status : %s", file.getStatus());
	}

	protected void checkUntrashable(SpaceFile file, String path) {
		checkModifiable(file);
		Preconditions.checkArgument(file.getStatus() == FileStatus.TRASHED, "cannot untrash file with status : %s", file.getStatus());
		if (findHealthyByPath(file.getOwner().getId(), path) != null) {
			throw new DuplicatePathException(file.getPath());
		}
	}

	protected void checkDeletable(SpaceFile file) {
		// //特殊文件夹（临时文件夹/备份文件夹）下的文件可以删除
		// //但不能做其他修改操作
		// if(file.getPath().startsWith(TEMP_PATH+"/") ||
		// file.getPath().startsWith(BACKUP_PATH+"/")){
		// return;
		// }
		checkModifiable(file);
	}

	private String concatPath(String path, String name) {
		String newPath = FilePathUtil.concatPath(path, name);
		if (newPath == null) {
			throw new InvalidPathException(path + "/" + name);
		}
		return newPath;
	}

	/**
	 * 对文件夹递归执行操作
	 * 
	 * @param folder
	 * @param action
	 */
	private void executeRecursively(SpaceFile folder, RecursiveAction action) {
		List<SpaceFile> children = ImmutableList.copyOf(folder.getChildren());
		for (SpaceFile child : children) {
			if (child.getIsDir()) {
				executeRecursively(child, action);
			}
			action.apply(child);
		}
	}

	protected void checkMoveable(SpaceFile file, String path, String name) {
		checkModifiable(file);
		if (path.startsWith(file.getPath() + "/")) {
			throw new InvalidDestinationPathException("cannot move to a chlid folder");
		}
		if (file.getStatus() == FileStatus.HEALTHY) {
			// healthy文件需要检查path是否冲突
			if (findHealthyByPath(file.getOwner().getId(), path) != null) {
				throw new DuplicatePathException(path);
			}
		} else if (file.getStatus() == FileStatus.ANCESTOR_TRASHED) {
			// ANCESTOR_TRASHED文件不允许移动
			throw new IllegalArgumentException("cannot move file with status ANCESTOR_TRASHED: " + file.getPath());
		}
	}

	protected void checkModifiable(SpaceFile file) {
		if (!file.getModifiable()) {
			throw new FileUnmodifiableException(file.getPath());
		}
	}

	private static interface RecursiveAction {
		void apply(SpaceFile file);
	}

	/**
	 * 检查文件扩展名是否禁止
	 * 
	 * @param filename
	 */
	public void checkFileExtenstion(String filename) {
		String ext = FilenameUtils.getExtension(filename);
		if (Strings.isNullOrEmpty(ext) == false) {
			String forbiddenExts = configService.getConfig(Configs.Keys.FORBIDDEN_EXT, "");
			for (String forbiddenExt : Splitter.on(',').split(forbiddenExts)) {
				if (forbiddenExt.equals(ext)) {
					throw new RestException(ErrorCode.FORBIDDEN_FILE_EXTENSION);
				}
			}
		}
	}

	/**
	 * 检查md5对应的文件是否存在
	 * 
	 * @param md5
	 */
	public void checkMd5FileExists(String md5) {
		if (runtimeContext.getFileStorageService().findFileSource(md5) == null) {
			throw new Md5FileNotFoundException();
		}
	}

	/**
	 * 将多个临时文件片段组合成为ByteSource
	 * 
	 * @author Chen Hao
	 */
	private class FileFragmentsByteSource extends ByteSource {
		List<String> fragmentMd5List;

		public FileFragmentsByteSource(List<String> fragmentMd5List) {
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

	public SpaceFileVersion findFileVersion(long fileVersionId) {
		return spaceFileVersionDao.findOne(fileVersionId);
	}

	@Transactional(readOnly = false)
	public <T extends BaseSpace> T save(T space) {
		return spaceDao.save(space);
	}

	@Transactional(readOnly = false)
	public void addTags(long fileId, long[] tags) {
		SpaceFile file = findFile(fileId);
		addTags(file, tags);
	}

	@Transactional(readOnly = false)
	public void addTags(SpaceFile file, long[] tags) {
		for (long tagId : tags) {
			SpaceTag tag = spaceTagService.findOne(tagId);
			if (tag != null && tag.getOwner().equals(file.getOwner())) {
				if (file.addTag(tag)) {
					spaceTagService.updateFilesCount(tagId, 1);
				}
			}
		}
		spaceFileDao.save(file);
	}

	@Transactional(readOnly = false)
	public void removeTags(long fileId, long[] tags) {
		SpaceFile file = findFile(fileId);
		removeTags(file, tags);
	}

	@Transactional(readOnly = false)
	public void removeTags(SpaceFile file, long[] tags) {
		for (long tagId : tags) {
			SpaceTag tag = spaceTagService.findOne(tagId);
			if (tag != null) {
				if (file.removeTag(tag)) {
					spaceTagService.updateFilesCount(tagId, -1);
				}
			}
		}
		spaceFileDao.save(file);
	}

	@Transactional(readOnly = false)
	public void removeTags(SpaceFile file, Collection<SpaceTag> tags) {
		for (SpaceTag tag : tags) {
			if (file.removeTag(tag)) {
				spaceTagService.updateFilesCount(tag.getId(), -1);
			}
		}
		spaceFileDao.save(file);
	}
}
