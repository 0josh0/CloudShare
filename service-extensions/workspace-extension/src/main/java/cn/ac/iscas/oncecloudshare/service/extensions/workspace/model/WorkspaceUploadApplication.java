package cn.ac.iscas.oncecloudshare.service.extensions.workspace.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import cn.ac.iscas.oncecloudshare.service.extensions.workspace.utils.WorkspaceUtils;

@Entity
@DiscriminatorValue(WorkspaceUtils.ApplicationTypes.UPLOAD)
public class WorkspaceUploadApplication extends WorkspaceApplication{

}
