package cn.ac.iscas.oncecloudshare.service.application.service;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

public interface ApplicationHandler {
	boolean canHandle(Application application);

	void preSave(Application application);
	
	void postSave(Application application);

	void preReview(Application application, ReviewApplication review, User master);

	void postReview(Application application, ReviewApplication review);

	void preCancel(Application application);

	void postCancel(Application application);
}