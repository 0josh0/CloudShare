package cn.ac.iscas.oncecloudshare.messaging.model.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.messaging.model.IdEntity;

@Entity
@Table (name="ocs_chat_conv",uniqueConstraints=
	@UniqueConstraint(columnNames={"userId","oppositeId"}))
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE)
public class Conversation extends IdEntity {

	protected Long userId;

	protected Long oppositeId;

	protected Long readSeq;

	protected Long maxSeq;

	public Conversation(){

	}

	public Conversation(Long userId, Long oppositeId, Long readSeq, Long maxSeq){
		this.userId=userId;
		this.oppositeId=oppositeId;
		this.readSeq=readSeq;
		this.maxSeq=maxSeq;
	}

	@Column(nullable=false,columnDefinition="BIGINT(11)")
	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId=userId;
	}

	@Column(nullable=false,columnDefinition="BIGINT(11)")
	public Long getOppositeId(){
		return oppositeId;
	}

	public void setOppositeId(Long oppositeId){
		this.oppositeId=oppositeId;
	}

	@Column(nullable=false,columnDefinition="BIGINT(11) DEFAULT 0")
	public Long getReadSeq(){
		return readSeq;
	}

	public void setReadSeq(Long readSeq){
		this.readSeq=readSeq;
	}

	@Column(nullable=false,columnDefinition="BIGINT(11) DEFAULT 0")
	public Long getMaxSeq(){
		return maxSeq;
	}

	public void setMaxSeq(Long maxSeq){
		this.maxSeq=maxSeq;
	}

}
