package cn.ac.iscas.oncecloudshare.service.service.filemeta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.DuplicatePathException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Service
@Transactional
public class FolderService extends BaseFileService{
	
	@Autowired
	FileService fileService;
	
	/**
	 * 查找文件夹，如果不存在返回null
	 * @return
	 */
	public File findFolder(long fileId){
		File file=find(fileId);
		if(file==null){
			return null;
		}
		return file.getIsDir()?file:null;
	}
	
	/**
	 * 查找文件夹，如果不存在抛出异常
	 * @param fileId
	 * @return
	 * @throws IllegalArgumentException
	 */
	public File findExistingFolder(long fileId) throws IllegalArgumentException{
		File file=findFolder(fileId);
		Preconditions.checkArgument(file!=null,"folder "+fileId+" not exists");
		return file;
	}
	
	/**
	 * 根目录
	 * @param ownerId
	 * @return
	 */
	public File findRoot(long ownerId){
		return findByPath(ownerId,ROOT_PATH,null).iterator().next();
	}
	
	/**
	 * 临时目录
	 * @param ownerId
	 * @return
	 */
	public File findTempDir(long ownerId){
		return findByPath(ownerId,TEMP_PATH,null).iterator().next();
	}
	
	/**
	 * 备份目录
	 * @param ownerId
	 * @return
	 */
	public File findBackupDir(long ownerId){
		return findByPath(ownerId,BACKUP_PATH,null).iterator().next();
	}
	
	/**
	 * 返回healthy的children
	 * @param parentId
	 * @param pageable
	 * @return
	 */
	public Page<File> findChildren(long parentId,Pageable pageable){
		return fDao.findHealthyChildren(parentId,pageable);
	}
	
	/**
	 * 新建文件夹
	 * @param ownerId
	 * @param parentId
	 * @param name
	 * @return
	 * @throws DuplicatePathException
	 */
	@Transactional
	public File makeFolder(long ownerId, long parentId, String name)
			throws IllegalArgumentException, DuplicatePathException{
		User owner=uService.findExistingUser(ownerId);
		File parent=findExistingFolder(parentId);
		
		String path=concatPath(parent.getPath(),name);
		System.out.println(path);
		if(findHealthyByPath(ownerId,path)!=null){
			throw new DuplicatePathException(path);
		}
		
		//save file meta
		File file=new File();
		file.setIsDir(true);
		file.setParent(parent);
		file.setPath(path);
		file.setStatus(FileStatus.HEALTHY);
		file.setOwner(owner);
		file.setVersionSeq(0);
		fDao.save(file);
		
		return file;
	}

	@Transactional
	public File makeFolder(long ownerId, String path) {
		File folder = findHealthyByPath(ownerId, path);
		if (folder != null){
			if (folder.getIsDir()){
				return folder;
			}
			throw new DuplicatePathException(path);
		}
		int index = path.lastIndexOf('/');
		String name = path.substring(index + 1);
		String parentPath = path.substring(0, index);
		if (parentPath.length() == 0){
			parentPath = FilePathUtil.ROOT_PATH;
		}
		File parent = makeFolder(ownerId, parentPath);
		return makeFolder(ownerId, parent.getId(), name);
	}
	
	/**
	 * 为新用户创建内置文件夹（如：根目录，临时目录，备份目录）
	 * @param ownerId
	 */
	@Transactional
	public void makeBuildinFolders(User owner){
		String[] paths={
				ROOT_PATH,TEMP_PATH,BACKUP_PATH
		};
		for(String path:paths){
			File file=new File();
			file.setModifiable(false);
			file.setIsDir(true);
			file.setParent(null);
			file.setPath(path);
			file.setStatus(FileStatus.HEALTHY);
			file.setOwner(owner);
			file.setVersionSeq(0);
			fDao.save(file);
		}
	}
	
	/**
	 * 移动文件夹
	 * @param fileId
	 * @param parentId
	 * @param name
	 */
	public void move(long fileId,long parentId,String name){
		File folder=findExistingFolder(fileId);
		File parent=findExistingFolder(parentId);
		
		final int oldPathLength=folder.getPath().length();
		final String newPath=concatPath(parent.getPath(),name);
		
		checkMoveable(folder,newPath,name);
		
		folder.setParent(parent);
		folder.setPath(newPath);
		fDao.save(folder);
		
		/*
		 * 递归修改子文件的path
		 */
		executeRecursively(folder,new RecursiveAction(){
			
			@Override
			public void apply(File file){
				
				file.setPath(newPath+file.getPath().substring(oldPathLength));
				fDao.save(file);
			}
		});
	}
	
	public void trash(long fileId){
		File folder=findExistingFolder(fileId);
		checkTrashable(folder);
		setStatusRecursively(folder,FileStatus.TRASHED,FileStatus.ANCESTOR_TRASHED);
	}
	
	public void untrash(long fileId){
		File folder=findExistingFolder(fileId);
		checkUntrashable(folder,folder.getPath());
		setStatusRecursively(folder,FileStatus.HEALTHY,FileStatus.HEALTHY);
	}
	
	public void untrashTo(long fileId,long parentId,String name){
		File folder=findExistingFolder(fileId);
		File parent=findExistingFolder(parentId);
		String path=concatPath(parent.getPath(),name);
		checkUntrashable(folder,path);
		move(fileId,parentId,name);
		setStatusRecursively(folder,FileStatus.HEALTHY,FileStatus.HEALTHY);
	}
	
	public void delete(long fileId){
		File folder=findExistingFolder(fileId);
		checkDeletable(folder);
		
		folder.setStatus(FileStatus.DELETED);
		fDao.save(folder);
		
		class LongWrapper{
			long value=0;
		}
		final LongWrapper totalSize=new LongWrapper();
		executeRecursively(folder,new RecursiveAction(){
			
			@Override
			public void apply(File file){
				if(file.getStatus()!=FileStatus.DELETED){
					file.setStatus(FileStatus.DELETED);
					fDao.save(file);
					totalSize.value=totalSize.value+file.getTotalSize();
				}
			}
		});
		System.out.println(totalSize.value);
		uService.incrRestQuota(folder.getOwner(),totalSize.value);
	}
	
	/**
	 * 清空某个用户的回收站 
	 *
	 * @param userId
	 */
	public void clearTrash(long userId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, userId));
		filters.add(new SearchFilter("status", Operator.EQ, FileStatus.TRASHED));
		List<File> files = fDao.findAll(Specifications.fromFilters(filters, File.class));
		for (File file : files) {
			file.setStatus(FileStatus.DELETED);
			fDao.save(file);
			if (file.getIsDir()){
				class LongWrapper{
					long value=0;
				}
				final LongWrapper totalSize=new LongWrapper();
				executeRecursively(file,new RecursiveAction(){
					
					@Override
					public void apply(File file){
						if(file.getStatus()!=FileStatus.DELETED){
							file.setStatus(FileStatus.DELETED);
							fDao.save(file);
							totalSize.value=totalSize.value+file.getTotalSize();
						}
					}
				});
				uService.incrRestQuota(file.getOwner(),totalSize.value);
			}
		}
	}
	

	/**
	 * 递归修改文件状态
	 * @param rootId 根目录id
	 * @param rootStatus 根目录状态
	 * @param childrenStatus 子文件状态
	 */
	private void setStatusRecursively(File folder,FileStatus rootStatus,
			final FileStatus childrenStatus){
		folder.setStatus(rootStatus);
		fDao.save(folder);
		
		executeRecursively(folder,new RecursiveAction(){
			
			@Override
			public void apply(File file){
				if(file.getStatus()!=FileStatus.DELETED){
					file.setStatus(childrenStatus);
					fDao.save(file);
				}
			}
		});
	}
	
	/**
	 * 对文件夹递归执行操作
	 * @param folder
	 * @param action
	 */
	private void executeRecursively(File folder,RecursiveAction action){
		List<File> children=ImmutableList.copyOf(folder.getChildren());
		for(File child:children){
			if(child.getIsDir()){
				executeRecursively(child,action);
			}
			action.apply(child);
		}
	}
	
	private static interface RecursiveAction{
		void apply(File file);
	}
}
