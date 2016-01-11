package cn.ac.iscas.oncecloudshare.messaging.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

@MappedSuperclass
public abstract class Message extends IdEntity {

	protected String content;

	protected Long ts;

	public Message(){
	}
	
	@PrePersist
	@Override
	protected void onCreate(){
		super.onCreate();
		if(ts==null){
			ts=System.currentTimeMillis();
		}
	}

	@NotEmpty
	@Length(min=1,max=1024)
//	@Column (nullable=false,length=1024)
	public String getContent(){
		return content;
	}

	public void setContent(String content){
		this.content=content;
	}

	@NotNull
	@Range(min=0)
	@Column(nullable=false,columnDefinition="BIGINT(15)")
	public Long getTs(){
		return ts;
	}

	public void setTs(Long ts){
		this.ts=ts;
	}

}
