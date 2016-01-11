package cn.ac.iscas.oncecloudshare.service.filestorage.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.filestorage.FileSource;

/**
 * 文件存储信息
 * 
 * @author Chen Hao
 */
@Entity
@Table(name="ocs_file_source")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class FileSourceImpl extends IdEntity
	implements FileSource{

	protected String md5;

	protected Long size;

	protected List<BlockAssociation> blocks;

	@Column(nullable=false,unique=true,columnDefinition="CHAR(32)")
	public String getMd5(){
		return md5;
	}

	public void setMd5(String md5){
		this.md5=md5;
	}

	@Column(nullable=false,columnDefinition="BIGINT(15)")
	public Long getSize(){
		return size;
	}

	public void setSize(Long size){
		this.size=size;
	}

	@OneToMany(mappedBy="fileSource",
			fetch=FetchType.EAGER,
			cascade=CascadeType.ALL)
	@OrderBy("seq asc")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<BlockAssociation> getBlocks(){
		return blocks;
	}

	public void setBlocks(List<BlockAssociation> blocks){
		this.blocks=blocks;
	}

}
