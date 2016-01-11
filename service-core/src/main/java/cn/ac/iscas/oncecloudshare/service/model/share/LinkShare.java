package cn.ac.iscas.oncecloudshare.service.model.share;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import cn.ac.iscas.oncecloudshare.service.utils.DateUtils;

@Entity
@Table(name = "ocs_share_link")
public class LinkShare extends AbstractShare {
	// 分享对应的唯一key
	private String key;
	// 下载文件使用的密码
	private String pass;
	// 失效时间
	private Date expireTime;
	// 下载次数
	private Long downloads = 0L;

	public LinkShare() {

	}

	@Temporal(TemporalType.DATE)
	public Date getExpireTime() {
		return expireTime;
	}

	@Transient
	public boolean isNeverExpired() {
		if (expireTime == null || expireTime.getTime() == DateUtils.NEVER_EXPIRE_MILLIS) {
			return true;
		}
		return false;
	}

	public void setNeverExpire() {
		this.expireTime = new Date(DateUtils.NEVER_EXPIRE_MILLIS);
	}

	@Transient
	public boolean isExpired() {
		if (isNeverExpired()) {
			return false;
		}
		return System.currentTimeMillis() > expireTime.getTime();
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	@Transient
	public boolean isNeedPass() {
		return StringUtils.isNotEmpty(pass);
	}

	public Long getDownloads() {
		return downloads;
	}

	public void setDownloads(Long downloads) {
		this.downloads = downloads;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	@Column(name = "externalKey", nullable = false, unique = true, updatable = false)
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
