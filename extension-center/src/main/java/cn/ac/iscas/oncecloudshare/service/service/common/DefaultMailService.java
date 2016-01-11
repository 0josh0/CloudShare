package cn.ac.iscas.oncecloudshare.service.service.common;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.model.common.Mail;

@Service
public class DefaultMailService implements MailService {

	private static Logger logger = LoggerFactory.getLogger(DefaultMailService.class);

	@Autowired
	ConfigService configService;

	ExecutorService executor;

	protected DefaultMailService() {
		int coreThreadCount = 1;
		int maxThreadCount = 10;
		int threadTimeoutSeconds = 60;
		this.executor = new ThreadPoolExecutor(coreThreadCount, maxThreadCount, threadTimeoutSeconds, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	public void send(String to, Mail mail) {
		executor.submit(new MailUnitOfWork(to, mail));
	}

	private class MailUnitOfWork implements Runnable {
		String to;
		Mail mail;

		public MailUnitOfWork(String to, Mail mail) {
			this.to = to;
			this.mail = mail;
		}

		@Override
		public void run() {
			final String account = configService.getConfig(Configs.Keys.MAIL_ACCOUNT, "");
			final String password = configService.getConfig(Configs.Keys.MAIL_PASSWORD, "");
			final String personal = configService.getConfig(Configs.Keys.MAIL_PERSONAL, "");
			String host = configService.getConfig(Configs.Keys.MAIL_HOST, "");
			String port = configService.getConfig(Configs.Keys.MAIL_PORT, "");

			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(account, password);
				}
			});
			try {
				MimeMessage message = new MimeMessage(session);
				message.setHeader("Content-Type", "text/html; charset=utf-8");
				message.setFrom(new InternetAddress(account, personal));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
				message.setSubject(mail.getSubject(), "UTF-8");
				message.setText(mail.getContent(), "UTF-8");
				Transport.send(message);
			} catch (Exception e) {
				logger.warn("fail to send email to " + to, e);
			}
		}
	}
}
