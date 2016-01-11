package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import cn.ac.iscas.oncecloudshare.service.application.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.WorkspaceApplication;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class WorkspaceApplicationDto extends ApplicationDto {
	public WorkspaceDto workspace;

	public static Function<WorkspaceApplication, WorkspaceApplicationDto> defaultTransformer = new Function<WorkspaceApplication, WorkspaceApplicationDto>() {
		@Override
		public WorkspaceApplicationDto apply(WorkspaceApplication input) {
			Preconditions.checkNotNull(input);
			WorkspaceApplicationDto output = defaultInit(input, new WorkspaceApplicationDto());
			if (input.getWorkspace() != null) {
				output.workspace = WorkspaceDto.glanceTransformer.apply(input.getWorkspace());
			}
			output.type = input.getType();
			return output;
		}
	};
}
