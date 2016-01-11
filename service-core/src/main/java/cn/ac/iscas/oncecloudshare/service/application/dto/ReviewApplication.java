package cn.ac.iscas.oncecloudshare.service.application.dto;

import org.hibernate.validator.constraints.Length;

public class ReviewApplication {
	// 是否通过，默认通过
	private Boolean agreed = Boolean.TRUE;
	// 审核意见
	@Length(max = 255)
	private String message;

	public Boolean getAgreed() {
		return agreed;
	}

	public void setAgreed(Boolean agreed) {
		this.agreed = agreed;
	}
}
