package cn.ac.iscas.oncecloudshare.service.model.filemeta;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name="ocs_file_version",uniqueConstraints=
	@UniqueConstraint(columnNames={"file_id","version"})
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class FileVersion extends GenericFileVersion<User>{

	@Override
	@NotNull
	@ManyToOne(optional=false)
	@JoinColumn(name="file_id")
	public File getFile(){
		return (File)file;
	}
}
