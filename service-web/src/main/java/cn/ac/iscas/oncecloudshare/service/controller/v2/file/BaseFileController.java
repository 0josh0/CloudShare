package cn.ac.iscas.oncecloudshare.service.controller.v2.file;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.Md5FileNotFoundException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FileService;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.FolderService;


public class BaseFileController extends BaseController{
	
	@Autowired
	protected FileService fileService;
	
	@Autowired
	protected FolderService folderService;

	protected File findFile(Long fileId){
		return findFile(fileId,true);
	}
	
	/**
	 * 查找目标文件夹，检查是否存在&是否有权限
	 * @param fileId
	 * @return
	 */
	protected File findFile(Long fileId,boolean checkOwner){
		File file=fileService.findFile(fileId);
		if(file==null){
			throw new RestException(ErrorCode.FILE_NOT_FOUND);
		}
		if(checkOwner && !file.getOwner().getId().equals(currentUserId())){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		return file;
	}
	
	protected File findParent(Long parentId){
		return findParent(parentId,true);
	}
	
	/**
	 * 查找父文件夹，检查是否存在，是否有权限
	 * @return
	 */
	protected File findParent(Long parentId,boolean checkOwner){
		File file=folderService.findFolder(parentId);
		if(file==null){
			throw new RestException(ErrorCode.PARENT_NOT_EXISTS);
		}
		if(checkOwner && !file.getOwner().getId().equals(currentUserId())){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		return file;
	}
	
	protected FileVersion findFileVersion(Long fileId,Integer ver){
		return findFileVersion(fileId,ver,true);
	}
	
	/**
	 * 查找文件版本，检查是否存在&是否有权限
	 * @param fileId
	 * @param ver
	 * @return
	 */
	protected FileVersion findFileVersion(Long fileId,Integer ver,boolean checkOwner){
		FileVersion fv=fileService.findFileVersion(fileId,ver);
		if(fv==null){
			throw new RestException(ErrorCode.FILE_VERSION_NOT_FOUND);
		}
		if(checkOwner && !fv.getFile().getOwner().getId().equals(currentUserId())){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		return fv;
	}
	
	protected FileVersion findFileVersion(Long fvId,boolean checkOwner){
		FileVersion fv=fileService.findFileVersion(fvId);
		if(fv==null){
			throw new RestException(ErrorCode.FILE_VERSION_NOT_FOUND);
		}
		if(checkOwner && !fv.getFile().getOwner().getId().equals(currentUserId())){
			throw new RestException(ErrorCode.FORBIDDEN);
		}
		return fv;
	}
	
	/**
	 * 检查文件扩展名是否禁止
	 * @param filename
	 */
	protected void checkFileExtenstion(String filename){
		String ext=FilenameUtils.getExtension(filename);
		if(Strings.isNullOrEmpty(ext)==false){
			String forbiddenExts=globalConfigService.getConfig(Configs.Keys.FORBIDDEN_EXT,"");
			for(String forbiddenExt:Splitter.on(',').split(forbiddenExts)){
				if(forbiddenExt.equals(ext)){
					throw new RestException(ErrorCode.FORBIDDEN_FILE_EXTENSION);
				}
			}
		}
	}
	
	/**
	 * 检查md5对应的文件是否存在
	 * @param md5
	 */
	protected void checkMd5FileExists(String md5){
		if(runtimeContext.getFileStorageService().findFileSource(md5)==null){
			throw new Md5FileNotFoundException();
		}
	}
}
