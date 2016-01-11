package cn.ac.iscas.oncecloudshare.service.model.groupshare;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFileVersion;

//@Entity
//@Table(name="ocs_group_file_version",uniqueConstraints=
//	@UniqueConstraint(columnNames={"file_id","version"})
//)
public class GroupFileVersion extends GenericFileVersion<ShareGroup>{

	@Override
	@ManyToOne(optional=false)
	@JoinColumn(name="file_id")
	public GroupFile getFile(){
		return (GroupFile)file;
	}
}
