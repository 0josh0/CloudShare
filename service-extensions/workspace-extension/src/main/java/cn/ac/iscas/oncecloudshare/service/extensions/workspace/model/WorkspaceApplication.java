package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.application.model.Application;

@Entity
@Table(name = "ocs_workspace_application")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class WorkspaceApplication extends Application {
	private Workspace workspace;
	private String type;

	@ManyToOne
	@JoinColumn(name = "workspace_id")
	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	@Column(nullable = false, insertable = false, updatable = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}