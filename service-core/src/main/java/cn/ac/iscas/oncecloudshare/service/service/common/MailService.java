package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.concurrent.Future;

import cn.ac.iscas.oncecloudshare.service.model.common.Mail;

public interface MailService {
	public Future<Boolean> send(String to, Mail mail);
}
