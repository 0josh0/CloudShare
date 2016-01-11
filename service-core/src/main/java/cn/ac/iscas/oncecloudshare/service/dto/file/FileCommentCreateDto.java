package cn.ac.iscas.oncecloudshare.service.dto.file;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FileCommentCreateDto {	
	@NotNull
	@Size(max = 1024)
	private String content;
	
	@Size(max = 20)
	private long[] at;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long[] getAt() {
		return at;
	}

	public void setAt(long[] at) {
		this.at = at;
	}
}
