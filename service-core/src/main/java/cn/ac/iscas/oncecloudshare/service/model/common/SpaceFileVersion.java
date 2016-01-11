package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFileVersion;

@Entity
@Table(name = "ocs_space_file_version")
public class SpaceFileVersion extends GenericFileVersion<BaseSpace> {
	private User creator;
	
	@Override
	@ManyToOne(optional = false)
	@JoinColumn(name = "file_id")
	public SpaceFile getFile() {
		return (SpaceFile) file;
	}

	@ManyToOne
	@JoinColumn(name = "create_by", updatable = false)
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}
}