package cn.ac.iscas.oncecloudshare.service.service.filemeta;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import cn.ac.iscas.oncecloudshare.service.dao.filemeta.FileDao;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.DuplicatePathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.FileUnmodifiableException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidDestinationPathException;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InvalidPathException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileStatus;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;


public class BaseFileService {
	
	public static final String ROOT_PATH="/";
	public static final String TEMP_PATH="_tmp";
	public static final String BACKUP_PATH="_bak";
	
	@Autowired
	protected RuntimeContext runtimeContext;

	@Autowired
	protected FileDao fDao;

	@Autowired
	protected UserService uService;
	
	
	/**
	 * 查找元数据
	 * @param fileId
	 * @return
	 */
	public File find(long fileId){
		File file=fDao.findOne(fileId);
		if(file!=null && file.getStatus()==FileStatus.DELETED){
			return null;
		}
		return file;
	}
	
	public File findExisting(long fileId){
		File file=find(fileId);
		if(file==null){
			throw new IllegalArgumentException("file not existing");
		}
		return file;
	}
	
	protected File findExisting(long fileId,boolean isDir){
		File file=find(fileId);
		if(file==null || file.getIsDir()!=isDir){
			throw new IllegalArgumentException("file not existing");
		}
		return file;
	}
	
	public Page<File> findByPath(long ownerId,String path,Pageable pageable){
		path=FilePathUtil.normalizePath(path);
		return fDao.findByOwnerIdAndPath(ownerId,path,pageable);
	}
	
	public Page<File> findByPathAndStatus(long ownerId,String path,
			FileStatus status,Pageable pageable){
		path=FilePathUtil.normalizePath(path);
		return fDao.findByOwnerIdAndPathAndStatus(ownerId,path,status,pageable);
	}
	
	public File findHealthyByPath(long ownerId,String path){
		Iterator<File> fileIterator=findByPathAndStatus(ownerId,
				path,FileStatus.HEALTHY,null).iterator();
		return fileIterator.hasNext()?fileIterator.next():null;
	}
	
	/**
	 * 搜索文件和文件夹
	 * @param ownerId
	 * @param query
	 * @param pageable
	 * @return
	 */
	public Page<File> search(long ownerId,String query,Pageable pageable){
		 try{
			List<SearchFilter> filters=SearchFilter.parseQuery(query);
			filters.add(new SearchFilter("owner.id",Operator.EQ,ownerId));
			filters.add(new SearchFilter("modifiable",Operator.EQ,true));
			filters.add(new SearchFilter("status",Operator.NE,FileStatus.DELETED));
			Specification<File> spec=Specifications.fromFilters(filters,File.class);
			return fDao.findAll(spec,pageable);
		}
		catch(Exception e){
			throw new SearchException(e.getLocalizedMessage());
		}
	}
	
	public List<File> findAll(List<SearchFilter> filters) {
		try {
			filters.add(new SearchFilter("modifiable", Operator.EQ, true));
			Specification<File> spec = Specifications.fromFilters(filters, File.class);
			return fDao.findAll(spec);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}
	
	public Page<File> findAll(List<SearchFilter> and, List<SearchFilter> or, Pageable pageable) {
		try {
			and.add(new SearchFilter("modifiable", Operator.EQ, true));
			Specification<File> spec = Specifications.fromFilters(and, or, File.class);
			return fDao.findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e.getLocalizedMessage());
		}
	}
	
	public void updateInfo(long fileId,String description,Boolean favorite){
		File file=findExisting(fileId);
		if(description!=null){
			file.setDescription(description);
		}
		if(favorite!=null){
			file.setFavorite(favorite);
		}
		fDao.save(file);
	}
	
	protected String concatPath(String basePath,String filename){
		String path=FilePathUtil.concatPath(basePath,filename);
		if(path==null){
			throw new InvalidPathException("invalid path: "+basePath+"/"+filename);
		}
		return path;
	}
	
	protected void checkModifiable(File file){
		if(file.getModifiable()==false){
			throw new FileUnmodifiableException(file.getPath());
		}
	}
	
	protected void checkMoveable(File file,String path,String name){
		checkModifiable(file);
		if(path.startsWith(file.getPath() + "/")){
			throw new InvalidDestinationPathException(
					"cannot move to a chlid folder");
		}
		if(file.getStatus()==FileStatus.HEALTHY){
			//healthy文件需要检查path是否冲突
			if(findHealthyByPath(file.getOwner().getId(),path)!=null){
				throw new DuplicatePathException(path);
			}
		}
		else if(file.getStatus()==FileStatus.ANCESTOR_TRASHED){
			//ANCESTOR_TRASHED文件不允许移动
			throw new IllegalArgumentException(
					"cannot move file with status ANCESTOR_TRASHED: "
							+file.getPath());
		}
	}
	
	protected void checkTrashable(File file){
		checkModifiable(file);
		if(file.getStatus()!=FileStatus.HEALTHY){
			throw new IllegalArgumentException(
					"cannot trash file with status "+file.getStatus());
		}
	}

	protected void checkUntrashable(File file,String path){
		checkModifiable(file);
		if(file.getStatus()!=FileStatus.TRASHED){
			throw new IllegalArgumentException(
					"cannot untrash file with status "+file.getStatus());
		}
		if(findHealthyByPath(file.getOwner().getId(),path)!=null){
			throw new DuplicatePathException(file.getPath());
		}
	}
	
	protected void checkDeletable(File file){
//		//特殊文件夹（临时文件夹/备份文件夹）下的文件可以删除
//		//但不能做其他修改操作
//		if(file.getPath().startsWith(TEMP_PATH+"/") ||
//				file.getPath().startsWith(BACKUP_PATH+"/")){
//			return;
//		}
		checkModifiable(file);
	}
}
