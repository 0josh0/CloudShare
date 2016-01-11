package cn.ac.iscas.oncecloudshare.service.dto.file;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UpdateTagOrderReq {
	@NotNull
	@Size(min = 1)
	private long[] tags;
	@NotNull
	@Size(min = 1)
	private int[] orders;

	public long[] getTags() {
		return tags;
	}

	public void setTags(long[] tags) {
		this.tags = tags;
	}

	public int[] getOrders() {
		return orders;
	}

	public void setOrders(int[] orders) {
		this.orders = orders;
	}
}
