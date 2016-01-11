package cn.ac.iscas.oncecloudshare.service.model.common;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.GenericFile;

import com.google.common.collect.Lists;

@Entity
@Table(name = "ocs_space_file")
public class SpaceFile extends GenericFile<BaseSpace> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6154642862049100983L;
	private User creator;
	// 收藏次数
	private long follows;
	// 下载次数
	private long downloads;
	private List<SpaceTag> tags = Lists.newArrayList();
	
	@Override
	@ManyToOne(optional=false)
	public BaseSpace getOwner(){
		return super.getOwner();
	}

	@Override
	@ManyToOne
	public SpaceFile getParent(){
		return (SpaceFile)super.getParent();
	}

	@SuppressWarnings("unchecked")
	@OneToMany(mappedBy="parent")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<SpaceFile> getChildren(){
		return (List<SpaceFile>)super.getChildren();
	}

	@SuppressWarnings("unchecked")
	@Override
	@OneToMany(mappedBy="file",fetch=FetchType.EAGER)
	@OrderBy("version DESC")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<SpaceFileVersion> getVersions(){
		return (List<SpaceFileVersion>)super.getVersions();
	}
	
	@Override
	@Transient
	public SpaceFileVersion getHeadVersion(){
		return (SpaceFileVersion)super.getHeadVersion();
	}
	
	@Override
	@Transient
	public SpaceFileVersion getVersion(int version){
		return (SpaceFileVersion)super.getVersion(version);
	}
	
	@Transient
	public Boolean getFavorite() {
		return false;
	}

	@ManyToOne
	@JoinColumn(name = "create_by", updatable = false)
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	@Column(updatable = false)
	public long getFollows() {
		return follows;
	}

	public void setFollows(long follows) {
		this.follows = follows;
	}	

	@Column(updatable = false)
	public long getDownloads() {
		return downloads;
	}

	public void setDownloads(long downloads) {
		this.downloads = downloads;
	}

	@ManyToMany
	@JoinTable(name = "ocs_space_tag_file", joinColumns = @JoinColumn(name = "file_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"file_id", "tag_id" }))
	public List<SpaceTag> getTags() {
		return tags;
	}

	public void setTags(List<SpaceTag> tags) {
		this.tags = tags;
	}

	public boolean addTag(SpaceTag tag) {
		if (tag.getOwner().equals(getOwner()) && tags.indexOf(tag) == -1) {
			tags.add(tag);
			return true;
		}
		return false;
	}

	public boolean removeTag(SpaceTag tag) {
		return tags.remove(tag);
	}
}
