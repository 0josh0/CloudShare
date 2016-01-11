package cn.ac.iscas.oncecloudshare.messaging.model.muc;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import cn.ac.iscas.oncecloudshare.messaging.model.IdEntity;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.GsonHidden;

@Entity
@Table (name="ocs_muc_room")
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE)
public class MucRoom extends IdEntity {

	protected String subject;

	protected Long owner;
	
	@GsonHidden
	protected Long maxSeq=0L;
	
	@GsonHidden
	protected Set<MucOccupant> occupants;
	
	protected Boolean special=Boolean.FALSE;

	@Column(length=50)
	public String getSubject(){
		return subject;
	}

	public void setSubject(String subject){
		this.subject=subject;
	}

	@Column(nullable=false,precision=11)
	public Long getOwner(){
		return owner;
	}
	
	public void setOwner(Long owner){
		this.owner=owner;
	}
	
	@Column (nullable=false,columnDefinition="BIGINT(11) DEFAULT 0")
	public Long getMaxSeq(){
		return maxSeq;
	}

	public void setMaxSeq(Long maxSeq){
		this.maxSeq=maxSeq;
	}

	@OneToMany(mappedBy="room")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public Set<MucOccupant> getOccupants(){
		return occupants;
	}
	
	public void setOccupants(Set<MucOccupant> occupants){
		this.occupants=occupants;
	}

	@Column(nullable=false,columnDefinition="TINYINT(1) default 0")
	public Boolean getSpecial(){
		return special;
	}
	
	public void setSpecial(Boolean special){
		this.special=special;
	}

}
