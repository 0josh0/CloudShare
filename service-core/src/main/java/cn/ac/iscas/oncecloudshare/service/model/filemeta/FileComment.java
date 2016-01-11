package cn.ac.iscas.oncecloudshare.service.model.filemeta;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;

import com.google.common.collect.Lists;

@Entity
@Table(name = "ocs_file_comment")
public class FileComment extends IdEntity {

	private User creater;

	private SpaceFile file;

	private String content;

	private List<User> at = Lists.newArrayList();

	public FileComment() {
		super();
	}

	public FileComment(User creater, SpaceFile file, String content, List<User> at) {
		super();
		this.creater = creater;
		this.file = file;
		this.content = content;
		this.at = at;
	}

	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	public User getCreater() {
		return creater;
	}

	public void setCreater(User creater) {
		this.creater = creater;
	}

	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	public SpaceFile getFile() {
		return file;
	}

	public void setFile(SpaceFile file) {
		this.file = file;
	}

	@Column(nullable = true, length = 1024)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@ManyToMany
	@JoinTable(name = "ocs_file_comment_at", joinColumns = @JoinColumn(name = "comment_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	public List<User> getAt() {
		return at;
	}

	public void setAt(List<User> at) {
		this.at = at;
	}
}
