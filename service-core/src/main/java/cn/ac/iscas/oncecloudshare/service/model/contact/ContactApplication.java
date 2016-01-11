package cn.ac.iscas.oncecloudshare.service.model.contact;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_contact_application")
public class ContactApplication extends Application {
	private User contact;

	@ManyToOne
	@JoinColumn(name = "contact_id")
	public User getContact() {
		return contact;
	}

	public void setContact(User contact) {
		this.contact = contact;
	}
}