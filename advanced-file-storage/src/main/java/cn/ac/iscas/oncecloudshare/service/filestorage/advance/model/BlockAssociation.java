package cn.ac.iscas.oncecloudshare.service.filestorage.advance.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

import com.google.common.base.Objects;

@Entity
@Table (name="ocs_block_association",uniqueConstraints=
	@UniqueConstraint(columnNames={"file_source_id","file_block_id","seq"})
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class BlockAssociation extends IdEntity {

	private FileSourceImpl fileSource;
	
	private FileBlock fileBlock;

	private Integer seq;
	
	public BlockAssociation(){
		
	}
	
	public BlockAssociation(FileSourceImpl fileSource,FileBlock fileBlock,int seq){
		setFileSource(fileSource);
		setFileBlock(fileBlock);
		this.seq=seq;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(fileSource.getId(),fileBlock.getId(),seq);
	}
	
	@Override
	public boolean equals(Object obj){
		if(this==obj){
			return true;
		}
		if(obj!=null && obj instanceof BlockAssociation){
			BlockAssociation that=(BlockAssociation)obj;
			return Objects.equal(fileSource.getId(),that.fileSource.getId())
					&& Objects.equal(fileBlock.getId(),that.fileBlock.getId())
					&& Objects.equal(seq,that.seq);
		}
		return false;
	}

	@ManyToOne
	@JoinColumn(name="file_source_id")
	public FileSourceImpl getFileSource(){
		return fileSource;
	}

	public void setFileSource(FileSourceImpl fileSource){
		this.fileSource=fileSource;
	}
	
	@ManyToOne
	@JoinColumn(name="file_block_id")
	public FileBlock getFileBlock(){
		return fileBlock;
	}
	
	public void setFileBlock(FileBlock fileBlock){
		this.fileBlock=fileBlock;
	}

	@Column(nullable=false)
	public Integer getSeq(){
		return seq;
	}

	public void setSeq(Integer seq){
		this.seq=seq;
	}

}
