package cn.ac.iscas.oncecloudshare.service.model.account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.utils.gson.GsonHidden;

@Entity
@Table(name="ocs_user_profile")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class UserProfile extends IdEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -408968119009020763L;

	@GsonHidden
	private User user;

	private Boolean male;
	private String wechatAccount;
	private String tel;
	private String workTel;
	
	@GsonHidden
	private String largeAvatar;
	@GsonHidden
	private String middleAvatar;
	@GsonHidden
	private String smallAvatar;
	
	@NotNull
	@OneToOne (optional=false)
	@JoinColumn (nullable=false,name="user_id",referencedColumnName="id")
	public User getUser(){
		return user;
	}

	public void setUser(User user){
		this.user=user;
	}

	@Column (columnDefinition="TINYINT(1)")
	public Boolean getMale(){
		return male;
	}

	public void setMale(Boolean male){
		this.male=male;
	}

	@Length(max=50)
//	@Column (length=50)
	public String getWechatAccount(){
		return wechatAccount;
	}

	public void setWechatAccount(String wechatAccount){
		this.wechatAccount=wechatAccount;
	}

	@Length(max=20)
//	@Column (length=20)
	public String getTel(){
		return tel;
	}

	public void setTel(String tel){
		this.tel=tel;
	}

	@Length(max=20)
//	@Column (length=20)
	public String getWorkTel(){
		return workTel;
	}

	public void setWorkTel(String workTel){
		this.workTel=workTel;
	}

	@Column(columnDefinition="CHAR(32)")
	public String getLargeAvatar(){
		return largeAvatar;
	}

	public void setLargeAvatar(String largeAvatar){
		this.largeAvatar=largeAvatar;
	}

	@Column(columnDefinition="CHAR(32)")
	public String getMiddleAvatar(){
		return middleAvatar;
	}

	public void setMiddleAvatar(String middleAvatar){
		this.middleAvatar=middleAvatar;
	}

	@Column(columnDefinition="CHAR(32)")
	public String getSmallAvatar(){
		return smallAvatar;
	}

	public void setSmallAvatar(String smallAvatar){
		this.smallAvatar=smallAvatar;
	}

}