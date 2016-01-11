package cn.ac.iscas.oncecloudshare.service.service.contact;

import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationHandlerAdapter;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.dao.contact.ContactApplicationDao;
import cn.ac.iscas.oncecloudshare.service.dao.contact.ContactDao;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.contact.Contact;
import cn.ac.iscas.oncecloudshare.service.model.contact.ContactApplication;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class ContactService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContactService.class);

	@Resource
	private ContactDao contactDao;
	@Resource
	private ContactApplicationDao contactApplicationDao;
	@Resource
	private ApplicationService applicationService;

	public ContactService() {
		super();

	}

	@PostConstruct
	public void init() {
		applicationService.addApplicationDao(ContactApplication.class, contactApplicationDao);
		applicationService.addApplicationHandler(new ApplicationHandlerAdapter() {
			@Override
			public boolean canHandle(Application application) {
				return application instanceof ContactApplication;
			}

			@Override
			public void preSave(Application application) {
				ContactApplication contactApplication = (ContactApplication) application;
				contactApplicationDao.cancel(application.getApplyBy().getId(), contactApplication.getContact().getId(), System.currentTimeMillis());
			}

			@Override
			public void preReview(Application application, ReviewApplication review, User master) {
				if (review.getAgreed()) {
					ContactApplication contactApplication = (ContactApplication) application;
					Contact contact = findOne(application.getApplyBy().getId(), contactApplication.getContact().getId());
					if (contact == null) {
						contact = new Contact(application.getApplyBy(), contactApplication.getContact());
						contactDao.save(contact);
						contact = new Contact(contactApplication.getContact(), application.getApplyBy());
						contactDao.save(contact);
					}
				}
			}
		});
	}

	public Contact findOne(long contactId) {
		return contactDao.findOne(contactId);
	}

	public Contact findOne(long ownerId, long contactId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("owner.id", Operator.EQ, ownerId));
		filters.add(new SearchFilter("contact.id", Operator.EQ, contactId));
		return contactDao.findOne(Specifications.fromFilters(filters, Contact.class));
	}

	public Page<Contact> findAll(Collection<SearchFilter> filters, Pageable pageable) {
		return contactDao.findAll(Specifications.fromFilters(filters, Contact.class), pageable);
	}

	public List<Contact> findAll(Collection<SearchFilter> filters) {
		return contactDao.findAll(Specifications.fromFilters(filters, Contact.class));
	}

	@Transactional(readOnly = false)
	public void delete(Contact contact) {
		contactDao.delete(contact.getOwner().getId(), contact.getContact().getId());
		contactDao.delete(contact.getContact().getId(), contact.getOwner().getId());
	}

	public ContactApplication findAvailableApplication(long applicantId, long contactId) {
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("applyBy.id", Operator.EQ, applicantId));
		filters.add(new SearchFilter("contact.id", Operator.EQ, contactId));
		filters.add(new SearchFilter("status", Operator.EQ, ApplicationStatus.TOREVIEW));
		return applicationService.findApplication(ContactApplication.class, filters);
	}
}