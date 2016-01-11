package cn.ac.iscas.oncecloudshare.service.model.contact;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

@Entity
@Table(name = "ocs_contact", uniqueConstraints = { @UniqueConstraint(columnNames = { "owner_id", "contact_id" }) })
public class Contact extends IdEntity {
	private User owner;
	private User contact;

	public Contact() {
		super();
	}

	public Contact(User owner, User contact) {
		super();
		this.owner = owner;
		this.contact = contact;
	}

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false, updatable = false)
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@ManyToOne
	@JoinColumn(name = "contact_id", nullable = false, updatable = false)
	public User getContact() {
		return contact;
	}

	public void setContact(User contact) {
		this.contact = contact;
	}
}
