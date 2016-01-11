package cn.ac.iscas.oncecloudshare.messaging.model.muc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Role;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.messaging.model.IdEntity;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.GsonHidden;

@Entity
@Table(name="ocs_muc_occupant",
	uniqueConstraints=@UniqueConstraint(columnNames={"room_id","userId"}))
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE)
public class MucOccupant extends IdEntity {

	@GsonHidden
	protected MucRoom room;

	protected Long userId;

	protected Role role;

	protected Long readSeq=0L;

	@ManyToOne(optional=false)
	@JoinColumn(name="room_id", nullable=false, updatable=false)
	public MucRoom getRoom(){
		return room;
	}

	public void setRoom(MucRoom room){
		this.room=room;
	}

	@Column(nullable=false,columnDefinition="BIGINT(11)")
	public Long getUserId(){
		return userId;
	}

	public void setUserId(Long userId){
		this.userId=userId;
	}

	@Enumerated(EnumType.STRING)
	@Column(length=20)
	public Role getRole(){
		return role;
	}

	public void setRole(Role role){
		this.role=role;
	}

	@Column (nullable=false,columnDefinition="BIGINT(11) DEFAULT 0")
	public Long getReadSeq(){
		return readSeq;
	}

	public void setReadSeq(Long readSeq){
		this.readSeq=readSeq;
	}

}
