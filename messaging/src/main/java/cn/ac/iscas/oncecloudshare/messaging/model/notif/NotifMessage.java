package cn.ac.iscas.oncecloudshare.messaging.model.notif;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import cn.ac.iscas.oncecloudshare.messaging.model.Message;


@Entity
@Table(name="ocs_notif_message")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class NotifMessage extends Message {
	
	protected Long receiver;
	
	private String type;
	
	protected Boolean readFlag=false;

	protected Boolean del=false;
	
	protected String attributes;
	
	public NotifMessage(){
	}
	
	public NotifMessage(String type,String content,String attributes){
		this.type=type;
		this.content=content;
		this.attributes=attributes;
	}
	
	@NotEmpty
	@Length(max=32)
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type=type;
	}

	@Column(nullable=false,columnDefinition="BIGINT(11)")
	public Long getReceiver(){
		return receiver;
	}

	public void setReceiver(Long receiver){
		this.receiver=receiver;
	}
	
	@Column(nullable=false, columnDefinition="TINYINT(1) DEFAULT 0")
	public Boolean getReadFlag(){
		return readFlag;
	}

	public void setReadFlag(Boolean readFlag){
		this.readFlag=readFlag;
	}
	
	@Column(nullable=false, columnDefinition="TINYINT(1) DEFAULT 0")
	public Boolean getDel(){
		return del;
	}

	public void setDel(Boolean del){
		this.del=del;
	}
	
	@NotNull
	@Length(min=0,max=1024)
	public String getAttributes(){
		return attributes;
	}

	public void setAttributes(String attributes){
		this.attributes=attributes;
	}

	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}
}
