package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.Roles;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseTeam;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;

@Entity
@DiscriminatorValue(WorkspaceUtils.DOMAIN)
public class WorkspaceTeam extends BaseTeam {
	private Workspace workspace;

	@OneToOne(optional = false, mappedBy = "team")
	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	@Transient
	public User getOwner(){
		for (TeamMate member : getMembers()){
			if (Roles.OWNER.equals(member.getRole())){
				return member.getUser();
			}
		}
		// 由于历史问题，有些workspace没有owner，自动把第一个用户提升为owner
		if (getMembers().size() > 0){
			TeamMate member = getMembers().get(0);
			member.setRole(Roles.OWNER);
			return member.getUser();
		}
		return null;
	}
}
