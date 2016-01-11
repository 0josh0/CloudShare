package cn.ac.iscas.oncecloudshare.messaging.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

@MappedSuperclass
public class AbstractChatMessage extends Message{

	protected Long sender;

	protected Long seq;

	protected ChatMessageType type;

	@Column(nullable=false,columnDefinition="BIGINT(11)")
	public Long getSender(){
		return sender;
	}

	public void setSender(Long sender){
		this.sender=sender;
	}

	@NotNull
	@Range(min=0)
	public Long getSeq(){
		return seq;
	}

	public void setSeq(Long seq){
		this.seq=seq;
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable=false,length=10)
	public ChatMessageType getType(){
		return type;
	}

	public void setType(ChatMessageType type){
		this.type=type;
	}

}
