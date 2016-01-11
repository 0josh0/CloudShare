package cn.ac.iscas.oncecloudshare.service.service.common;

import cn.ac.iscas.oncecloudshare.service.model.common.Mail;


public interface MailService{

	public void send(String to, Mail mail);
}
