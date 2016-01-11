package cn.ac.iscas.oncecloudshare.service.dao.contact;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cn.ac.iscas.oncecloudshare.service.application.dao.ApplicationDao;
import cn.ac.iscas.oncecloudshare.service.model.contact.ContactApplication;

public interface ContactApplicationDao extends ApplicationDao<ContactApplication> {
	@Modifying
	@Query("UPDATE ContactApplication SET status = 'CANCELED', cancelAt = ?3 WHERE applyBy.id = ?1 AND contact.id = ?2 AND status = 'TOREVIEW'")
	void cancel(long applicantId, long contactId, long cancelAt);
}