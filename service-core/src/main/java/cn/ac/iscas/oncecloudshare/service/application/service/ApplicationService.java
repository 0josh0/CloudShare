package cn.ac.iscas.oncecloudshare.service.application.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.service.application.dao.ApplicationDao;
import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.exceptions.SearchException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.Specifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
@Transactional(readOnly = true)
public class ApplicationService {
	private Map<Class<?>, ApplicationDao<?>> daos = Maps.newHashMap();
	private List<ApplicationHandler> handlers = Lists.newArrayList();

	public ApplicationService() {
	}

	@SuppressWarnings("unchecked")
	protected <T extends Application> ApplicationDao<T> getApplicationDao(Class<T> clazz) {
		return (ApplicationDao<T>) daos.get(clazz);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Application> ApplicationDao<T> getApplicationDao(T application) {
		return (ApplicationDao<T>) getApplicationDao(application.getClass());
	}

	public <T extends Application> void addApplicationDao(Class<T> clazz, ApplicationDao<T> dao) {
		daos.put(clazz, dao);
	}

	public void addApplicationHandler(ApplicationHandler handler) {
		if (handlers.indexOf(handler) == -1) {
			handlers.add(handler);
		}
	}

	@Transactional(readOnly = false)
	public <T extends Application> T save(T application) {
		for (ApplicationHandler handler : handlers) {
			if (handler.canHandle(application)) {
				handler.preSave(application);
			}
		}
		getApplicationDao(application).save(application);

		for (ApplicationHandler handler : handlers) {
			if (handler.canHandle(application)) {
				handler.postSave(application);
			}
		}
		return application;
	}

	/**
	 * 审核申请
	 * 
	 * @param application
	 * @param request
	 * @param master
	 */
	@Transactional(readOnly = false)
	public void reviewApplication(Application application, ReviewApplication request, User master) {
		for (ApplicationHandler handler : handlers) {
			if (handler.canHandle(application)) {
				handler.preReview(application, request, master);
			}
		}
		application.setStatus(request.getAgreed() ? ApplicationStatus.AGREED : ApplicationStatus.DISAGREED);
		application.setReviewAt(System.currentTimeMillis());
		application.setReviewBy(master);
		application.setReviewContentObject(request);
		getApplicationDao(application).save(application);

		for (ApplicationHandler handler : handlers) {
			if (handler.canHandle(application)) {
				handler.postReview(application, request);
			}
		}
	}

	/**
	 * 取消申请
	 * 
	 * @param application
	 */
	@Transactional(readOnly = false)
	public void cancelApplication(Application application) {
		for (ApplicationHandler handler : handlers) {
			if (handler.canHandle(application)) {
				handler.preCancel(application);
			}
		}
		application.setStatus(ApplicationStatus.CANCELED);
		application.setCancelAt(System.currentTimeMillis());
		getApplicationDao(application).save(application);
		for (ApplicationHandler handler : handlers) {
			if (handler.canHandle(application)) {
				handler.postCancel(application);
			}
		}
	}

	public <T extends Application> T findOne(Class<T> applicationClass, long id) {
		return getApplicationDao(applicationClass).findOne(id);
	}

	public <T extends Application> T findApplication(Class<T> applicationClass, Collection<SearchFilter> filters) {
		List<T> list =  getApplicationDao(applicationClass).findAll(Specifications.fromFilters(filters, applicationClass));
		if (list == null || list.size() == 0){
			return null;
		}
		return list.get(0);
	}

	public <T extends Application> Page<T> findApplications(Class<T> applicationClass, Collection<SearchFilter> filters, Pageable pageable) {
		try {
			Specification<T> spec = Specifications.fromFilters(filters, applicationClass);
			return getApplicationDao(applicationClass).findAll(spec, pageable);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
}