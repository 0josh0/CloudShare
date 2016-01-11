package cn.ac.iscas.oncecloudshare.service.model.filemeta;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springside.modules.utils.Collections3;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;
import cn.ac.iscas.oncecloudshare.service.utils.FilePathUtil;
import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

/**
 * 
 * 
 * @author Chen Hao
 * @param <T> 文件所有者的类型
 */
@MappedSuperclass
public class GenericFile <T extends FileOwner> extends DescriptionEntity{
	
	protected T owner;

	protected String path;

	protected String name;

	protected Boolean isDir;
	
	protected Boolean favorite=false;
	
	protected Boolean modifiable=true;

	protected String mimeType;

	protected FileStatus status;

	@GsonHidden
	protected Integer versionSeq;
	
	@GsonHidden
	protected GenericFile<T> parent;

	@GsonHidden
	protected List<? extends GenericFile<T>> children;

	protected List<? extends GenericFileVersion<T>> versions;
	
	@Transient
	public T getOwner(){
		return owner;
	}

	public void setOwner(T owner){
		this.owner=owner;
	}

	@NotNull
	@Length(min=1,max=512)
//	@Column(nullable=false,length=512)
	public String getPath(){
		return path;
	}

	public void setPath(String path){
		path=FilePathUtil.normalizePath(path);
		if(path!=null){
			this.name=FilePathUtil.extractFilenameFromPath(path);
		}
		this.path=path;
	}

	@NotNull
//	@Length(min=1,max=64)
//	@Pattern(regexp=FILE_NAME_REGEX)
	@Column(nullable=false,length=64)
	public String getName(){
		return name;
	}

	protected void setName(String name){
		this.name=name;
	}

	@NotNull
	@Column(nullable=false,columnDefinition="TINYINT(1)")
	public Boolean getIsDir(){
		return isDir;
	}

	public void setIsDir(Boolean isDir){
		this.isDir=isDir;
	}

	@NotNull
	@Column(nullable=false,columnDefinition="TINYINT(1) DEFAULT 0")
	public Boolean getFavorite(){
		return favorite;
	}
	
	public void setFavorite(Boolean favorite){
		this.favorite=favorite;
	}

	@NotNull
	@Column(nullable=false,columnDefinition="TINYINT(1) DEFAULT 1")
	public Boolean getModifiable(){
		return modifiable;
	}

	public void setModifiable(Boolean modifiable){
		this.modifiable=modifiable;
	}

	@Length(max=32)
//	@Column(length=32)
	public String getMimeType(){
		return mimeType;
	}

	public void setMimeType(String mimeType){
		this.mimeType=mimeType;
	}

	@NotNull
	@Column(length=16)
	@Enumerated(EnumType.STRING)
	public FileStatus getStatus(){
		return status;
	}

	public void setStatus(FileStatus status){
		this.status=status;
	}

	@Transient
	public GenericFile<T> getParent(){
		return parent;
	}

	public void setParent(GenericFile<T> parent){
		this.parent=parent;
	}

	@Transient
	public List<? extends GenericFile<T>> getChildren(){
		return children;
	}

	public void setChildren(List<? extends GenericFile<T>> children){
		this.children=children;
	}

	@Transient
	public List<? extends GenericFileVersion<T>> getVersions(){
		return versions;
	}

	public void setVersions(List<? extends GenericFileVersion<T>> versions){
		this.versions=versions;
	}

	@Column(nullable=false,columnDefinition="INT(5) DEFAULT 0")
	public Integer getVersionSeq(){
		return versionSeq;
	}

	public void setVersionSeq(Integer versionSeq){
		this.versionSeq=versionSeq;
	}

	/**
	 * 最新版本
	 * @return
	 */
	@Transient
	public GenericFileVersion<T> getHeadVersion(){
		if(Collections3.isNotEmpty(versions)){
			return versions.get(0);
		}
		return null;
	}
	
	@Transient
	public GenericFileVersion<T> getVersion(int version){
		if(Collections3.isNotEmpty(versions)){
			for(GenericFileVersion<T> fv:versions){
				if(fv.getVersion().equals(version)){
					return fv;
				}
			}
		}
		return null;
	}
	
	/**
	 * 所有版本的size之和
	 * @return
	 */
	@Transient
	public long getTotalSize(){
		long size=0;
		if(versions!=null){
			for(GenericFileVersion<T> fv:versions){
				size+=fv.getSize();
			}
		}
		return size;
	}
	
	/**
	 * 判断当前文件是否是另一个文件的子文件
	 * 
	 * @param parent
	 * @return
	 */
	@Transient
	public boolean isChildOf(File parent){
		if (!parent.isDir){
			return false;
		}
		String parentPath = parent.getPath();
		if (!parentPath.endsWith("/")){
			parentPath = parentPath.concat("/");
		}
		return getPath().startsWith(parentPath);
	}
	
	@Transient
	public int getChildrenCount(FileStatus status){
		if (!getIsDir()){
			return 0;
		}
		int count = 0;
		if (getChildren() != null){
			for (GenericFile<? extends FileOwner> child : getChildren()){
				if (child.getStatus().equals(status)){
					count++;
				}
			}
		}
		return count;
	}
}
