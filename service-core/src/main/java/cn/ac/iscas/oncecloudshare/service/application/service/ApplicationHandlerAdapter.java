package cn.ac.iscas.oncecloudshare.service.application.service;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.model.account.User;

public class ApplicationHandlerAdapter implements ApplicationHandler {
	@Override
	public boolean canHandle(Application application) {
		return false;
	}
	
	@Override
	public void preSave(Application application) {
	}

	@Override
	public void postSave(Application application) {
	}

	@Override
	public void preReview(Application application, ReviewApplication review, User master) {
	}

	@Override
	public void postReview(Application application, ReviewApplication review) {
	}

	@Override
	public void preCancel(Application application) {
	}

	@Override
	public void postCancel(Application application) {
	}
}
