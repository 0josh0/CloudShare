package cn.ac.iscas.oncecloudshare.service.application.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.utils.DateUtils;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@MappedSuperclass
public abstract class Application {
	protected Long id;
	// 申请人
	private User applyBy;
	// 申请时间
	private Long applyAt;
	// 申请内容
	private String content;
	// 审核人
	private User reviewBy;
	// 审核时间
	private Long reviewAt;
	// 审核内容
	private String reviewContent;
	// 撤销申请时间
	private Long cancelAt;
	// 失效时间
	private long expireAt = DateUtils.NEVER_EXPIRE_MILLIS;
	// 申请状态
	private ApplicationStatus status;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(length = 255, updatable = false)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Transient
	public <T> T getContentObject(Class<T> clazz) {
		if (Strings.isNullOrEmpty(content)) {
			return null;
		}
		return Gsons.defaultGsonNoPrettify().fromJson(this.content, clazz);
	}

	public void setContentObject(Object content) {
		this.content = Gsons.defaultGsonNoPrettify().toJson(content);
	}

	@ManyToOne
	@JoinColumn(name = "apply_by", nullable = false, updatable = false)
	public User getApplyBy() {
		return applyBy;
	}

	public void setApplyBy(User applyBy) {
		this.applyBy = applyBy;
	}

	@Column(updatable = false)
	public Long getApplyAt() {
		return applyAt;
	}

	public void setApplyAt(Long applyAt) {
		this.applyAt = applyAt;
	}

	public Long getReviewAt() {
		return reviewAt;
	}

	public void setReviewAt(Long reviewAt) {
		this.reviewAt = reviewAt;
	}

	public long getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(long expireAt) {
		this.expireAt = expireAt;
	}

	@ManyToOne
	@JoinColumn(name = "review_by")
	public User getReviewBy() {
		return reviewBy;
	}

	public void setReviewBy(User reviewBy) {
		this.reviewBy = reviewBy;
	}

	public String getReviewContent() {
		return reviewContent;
	}

	public void setReviewContent(String reviewContent) {
		this.reviewContent = reviewContent;
	}

	@Transient
	public <T> T getReviewContentObject(Class<T> clazz) {
		if (Strings.isNullOrEmpty(reviewContent)) {
			return null;
		}
		return Gsons.defaultGsonNoPrettify().fromJson(this.reviewContent, clazz);
	}

	public void setReviewContentObject(Object content) {
		this.reviewContent = Gsons.defaultGsonNoPrettify().toJson(content);
	}

	@Column(length = 16, nullable = false)
	@Enumerated(EnumType.STRING)
	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public Long getCancelAt() {
		return cancelAt;
	}

	public void setCancelAt(Long cancelAt) {
		this.cancelAt = cancelAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || id == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Application other = (Application) obj;
		return id.equals(other.id);
	}

	public static <T extends Application> T defaultInit(T model, User applyBy, Object applyContent) {
		Preconditions.checkNotNull(model);
		model.setApplyBy(applyBy);
		model.setApplyAt(System.currentTimeMillis());
		if (applyContent != null) {
			model.setContentObject(applyContent);
		}
		model.setStatus(ApplicationStatus.TOREVIEW);
		return model;
	}
}