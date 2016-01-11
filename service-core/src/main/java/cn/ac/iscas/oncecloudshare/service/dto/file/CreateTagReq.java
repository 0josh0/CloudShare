package cn.ac.iscas.oncecloudshare.service.dto.file;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateTagReq {
	@NotNull
	@Size(min = 1, max = 32)
	private String title;
	private int orderIndex;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}
}
