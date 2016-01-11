package cn.ac.iscas.oncecloudshare.service.model.filemeta;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import cn.ac.iscas.oncecloudshare.service.model.account.User;

import com.google.common.collect.Lists;

@Entity
@Table(name = "ocs_file_meta")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class File extends GenericFile<User> {
	private List<Tag> tags = Lists.newArrayList();

	@Override
	@NotNull
	@ManyToOne(optional = false)
	public User getOwner() {
		return super.getOwner();
	}

	@Override
	@ManyToOne()
	public File getParent() {
		return (File) super.getParent();
	}

	@SuppressWarnings("unchecked")
	@Override
	@OneToMany(mappedBy = "parent")
	@OnDelete(action = OnDeleteAction.CASCADE)
	public List<File> getChildren() {
		return (List<File>) super.getChildren();
	}

	@SuppressWarnings("unchecked")
	@Override
	@OneToMany(mappedBy = "file", fetch = FetchType.EAGER)
	@OrderBy("version DESC")
	@OnDelete(action = OnDeleteAction.CASCADE)
	public List<FileVersion> getVersions() {
		return (List<FileVersion>) super.getVersions();
	}

	@Override
	@Transient
	public FileVersion getHeadVersion() {
		return (FileVersion) super.getHeadVersion();
	}

	@Override
	@Transient
	public FileVersion getVersion(int version) {
		return (FileVersion) super.getVersion(version);
	}

	@ManyToMany
	@JoinTable(name = "ocs_tag_file", joinColumns = @JoinColumn(name = "file_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"file_id", "tag_id" }))
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public boolean addTag(Tag tag) {
		if (tag.getOwner().equals(getOwner()) && tags.indexOf(tag) == -1) {
			tags.add(tag);
			return true;
		}
		return false;
	}

	public boolean removeTag(Tag tag) {
		return tags.remove(tag);
	}
}
