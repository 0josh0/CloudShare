package cn.ac.iscas.oncecloudshare.service.extensions.company.space.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import cn.ac.iscas.oncecloudshare.service.model.DescriptionEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_company_space_application")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 30)
public class CompanySpaceApplication extends DescriptionEntity {
	// 申请人
	private User applicant;
	// 审核人
	private User reviewBy;
	// 审核时间
	private Date reviewTime;
	// 审核的消息
	private String reviewMessage;
	// 审核的内容
	private String reviewContent;

	// 申请的状态
	private ApplicationStatus status;

	@ManyToOne
	@JoinColumn(name = "applicant_id")
	public User getApplicant() {
		return applicant;
	}

	public void setApplicant(User applicant) {
		this.applicant = applicant;
	}

	@ManyToOne
	@JoinColumn(name = "review_by")
	public User getReviewBy() {
		return reviewBy;
	}

	public void setReviewBy(User reviewBy) {
		this.reviewBy = reviewBy;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}

	@Column(length = 255)
	public String getReviewMessage() {
		return reviewMessage;
	}

	public void setReviewMessage(String reviewMessage) {
		this.reviewMessage = reviewMessage;
	}

	@Column(length = 16, nullable = false)
	@Enumerated(EnumType.STRING)
	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public String getReviewContent() {
		return reviewContent;
	}

	public void setReviewContent(String reviewContent) {
		this.reviewContent = reviewContent;
	}
}
