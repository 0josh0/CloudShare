package cn.ac.iscas.oncecloudshare.service.model.filemeta;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

@MappedSuperclass
public class GenericFileVersion <T extends FileOwner> extends IdEntity {

	@GsonHidden
	protected GenericFile<T> file;
	protected Integer version;
	protected Long size;
	protected String md5;

	@Transient
	public GenericFile<T> getFile(){
		return file;
	}
	
	public void setFile(GenericFile<T> file){
		this.file=file;
	}

	@NotNull
//	@Column(nullable=false)
	public Integer getVersion(){
		return version;
	}

	public void setVersion(Integer version){
		this.version=version;
	}

	@NotNull
	@Range(min=0)
//	@Column(nullable=false)
	public Long getSize(){
		return size;
	}

	public void setSize(Long size){
		this.size=size;
	}

	@NotNull
	@Column(nullable=false,columnDefinition="CHAR(32)")
	public String getMd5(){
		return md5;
	}

	public void setMd5(String md5){
		this.md5=md5;
	}

}
