package cn.ac.iscas.oncecloudshare.messaging.model.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.messaging.model.AbstractChatMessage;

@Entity
@Table(name="ocs_chat_message")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ChatMessage extends AbstractChatMessage {

	private Long receiver;

	private Boolean senderDel=false;

	private Boolean receiverDel=false;

	@Column(nullable=false,columnDefinition="TINYINT(1) DEFAULT 0")
	public Boolean getSenderDel(){
		return senderDel;
	}

	public void setSenderDel(Boolean senderDel){
		this.senderDel=senderDel;
	}

	@Column(nullable=false,columnDefinition="TINYINT(1) DEFAULT 0")
	public Boolean getReceiverDel(){
		return receiverDel;
	}

	public void setReceiverDel(Boolean receiverDel){
		this.receiverDel=receiverDel;
	}
	
	@Column (nullable=false,columnDefinition="BIGINT(11)")
	public Long getReceiver(){
		return receiver;
	}

	public void setReceiver(Long receiver){
		this.receiver=receiver;
	}

}
