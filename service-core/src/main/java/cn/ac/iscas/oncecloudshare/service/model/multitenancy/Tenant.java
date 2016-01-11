package cn.ac.iscas.oncecloudshare.service.model.multitenancy;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Plan.Type;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;

@Entity
@Table (name="ocs_tenant",schema=Constants.GLOBAL_SCHEMA)
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE)
public class Tenant extends IdEntity {

	@Transient
	private static final String TABLE_NAME = "ocs_tenant";

	private String name;

	private String email;

	private String password;
	
	private String salt;

	private TenantStatus status;

	private String contacts;

	private String contactEmail;

	private String phone;

	private Date registerAt;

	private Date activateAt;

	private Date expireAt;

	private Type plan;

	private Integer members;

	private Long quota;

	private String industry;

	private String scale;

	private String address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Enumerated(EnumType.STRING)
	public TenantStatus getStatus() {
		return status;
	}

	public void setStatus(TenantStatus status) {
		this.status = status;
	}

	public String getContacts() {
		return contacts;
	}

	public void setContacts(String contacts) {
		this.contacts = contacts;
	}

	public Date getRegisterAt() {
		return registerAt;
	}

	public void setRegisterAt(Date registerAt) {
		this.registerAt = registerAt;
	}

	public Date getActivateAt() {
		return activateAt;
	}

	public void setActivateAt(Date activateAt) {
		this.activateAt = activateAt;
	}

	public Date getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(Date expireAt) {
		this.expireAt = expireAt;
	}

	@Enumerated(EnumType.STRING)
	public Type getPlan() {
		return plan;
	}

	public void setPlan(Type plan) {
		this.plan = plan;
	}

	public Integer getMembers() {
		return members;
	}

	public void setMembers(Integer members) {
		this.members = members;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Long getQuota() {
		return quota;
	}

	public void setQuota(Long quota) {
		this.quota = quota;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
