package cn.ac.iscas.oncecloudshare.service.dto.contact;

import javax.validation.constraints.Size;

public class AddContactReq {
	private long userId;
	@Size(max = 64)
	private String intro;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}
}
