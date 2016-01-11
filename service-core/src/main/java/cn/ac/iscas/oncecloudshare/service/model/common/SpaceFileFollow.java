package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_space_file_follow", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "file_id" }) })
public class SpaceFileFollow extends IdEntity {
	public SpaceFileFollow() {
		super();
	}

	public SpaceFileFollow(User user, SpaceFile file) {
		super();
		this.user = user;
		this.file = file;
	}

	private User user;
	private SpaceFile file;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne
	@JoinColumn(name = "file_id", nullable = false)
	public SpaceFile getFile() {
		return file;
	}

	public void setFile(SpaceFile file) {
		this.file = file;
	}
}
