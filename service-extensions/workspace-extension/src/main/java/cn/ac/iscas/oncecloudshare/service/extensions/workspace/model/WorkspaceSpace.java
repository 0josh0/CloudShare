package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;

@Entity
@DiscriminatorValue(WorkspaceUtils.DOMAIN)
public class WorkspaceSpace extends BaseSpace implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7672792249566777277L;
	private Workspace workspace;

	@OneToOne(optional = false, mappedBy = "space", fetch = FetchType.LAZY)
	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
