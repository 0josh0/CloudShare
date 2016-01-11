package cn.ac.iscas.oncecloudshare.messaging.model.muc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.messaging.model.AbstractChatMessage;

@Entity
@Table (name="ocs_muc_message")
@Cache (usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MucMessage extends AbstractChatMessage {

	private Long roomId;

//	private Boolean deleted;

	@Column (nullable=false,columnDefinition="BIGINT(11)")
	public Long getRoomId(){
		return roomId;
	}

	public void setRoomId(Long roomId){
		this.roomId=roomId;
	}

}
