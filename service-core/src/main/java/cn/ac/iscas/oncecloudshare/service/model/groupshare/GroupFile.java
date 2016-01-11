package cn.ac.iscas.oncecloudshare.service.model.groupshare;

import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFile;

//@Entity
//@Table(name="ocs_group_file")
public class GroupFile extends GenericFile<ShareGroup>{

	@Override
	@ManyToOne(optional=false)
	public ShareGroup getOwner(){
		return super.getOwner();
	}

	@Override
	@ManyToOne
	public GroupFile getParent(){
		return (GroupFile)super.getParent();
	}

	@Override
	@OneToMany(mappedBy="parent")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<GroupFile> getChildren(){
		return (List<GroupFile>)super.getChildren();
	}

	@Override
	@OneToMany(mappedBy="file",fetch=FetchType.EAGER)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<GroupFileVersion> getVersions(){
		return (List<GroupFileVersion>)super.getVersions();
	}
}
