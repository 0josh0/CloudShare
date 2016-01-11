package cn.ac.iscas.oncecloudshare.service.extensions.workspace.dto;

import javax.validation.constraints.Pattern;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;

public class ReviewJoin extends ReviewApplication {
	@Pattern(regexp = "reader|limited_writer|writer|admin")
	private String role;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
