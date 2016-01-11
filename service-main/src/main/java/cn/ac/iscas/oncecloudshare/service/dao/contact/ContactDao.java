package cn.ac.iscas.oncecloudshare.service.dao.contact;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.model.contact.Contact;

public interface ContactDao extends PagingAndSortingRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {
	@Modifying
	@Query("DELETE FROM Contact WHERE owner.id = ?1 AND contact.id = ?2")
	void delete(long ownerId, long contactId);
}