package cn.ac.iscas.oncecloudshare.service.service.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.annotation.Resource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail.Attachment;

@Service
public class DefaultMailService implements MailService {

	private static Logger logger = LoggerFactory.getLogger(DefaultMailService.class);

	@Resource(name="globalConfigService")
	private ConfigService<?> configService;

	ExecutorService executor;

	protected DefaultMailService() {
		int coreThreadCount = 1;
		int maxThreadCount = 10;
		int threadTimeoutSeconds = 60;
		this.executor = new ThreadPoolExecutor(coreThreadCount, maxThreadCount, threadTimeoutSeconds, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	public Future<Boolean> send(String to, Mail mail) {
		return executor.submit(new MailUnitOfWork(to, mail));
	}

	private class MailUnitOfWork implements Callable<Boolean> {
		String to;
		Mail mail;

		public MailUnitOfWork(String to, Mail mail) {
			this.to = to;
			this.mail = mail;
		}

		@Override
		public Boolean call() throws Exception {
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
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(account, personal));
				InternetAddress[] recips = InternetAddress.parse(to, false);
			    for(int i=0; i<recips.length; i++) {
			        try {
			            recips[i] = new InternetAddress(recips[i].getAddress(), recips[i].getPersonal(), "utf-8");
			        } catch(UnsupportedEncodingException uee) {
			            throw new RuntimeException("utf-8 not valid encoding?", uee);
			        }
			    }
				message.setRecipients(Message.RecipientType.TO, recips);
				message.setSubject(MimeUtility.encodeText(mail.getSubject(), "UTF-8", "B"));

				// 如果需要发送附件
				if (mail.getAttachments().size() > 0) {
					Multipart multipart = new MimeMultipart();
					// 设置邮件的文本内容
					BodyPart contentPart = new MimeBodyPart();
					contentPart.setContent(mail.getContent(), mail.getType());
					multipart.addBodyPart(contentPart);
					// 添加附件
					for (final Attachment attachment : mail.getAttachments()) {
						
						BodyPart attachmentPart = new MimeBodyPart();
						DataSource source = new DataSource() {
							@Override
							public OutputStream getOutputStream() throws IOException {
								throw new UnsupportedOperationException();
							}

							@Override
							public String getName() {
								return attachment.name;
							}

							@Override
							public InputStream getInputStream() throws IOException {
								return attachment.source.openStream();
							}

							@Override
							public String getContentType() {
								return FileTypeMap.getDefaultFileTypeMap().getContentType(attachment.name);
							}
						};
						// 添加附件的内容
						attachmentPart.setDataHandler(new DataHandler(source));
						// 添加附件的标题
						attachmentPart.setFileName(MimeUtility.encodeText(attachment.name, "UTF-8", "B"));
						multipart.addBodyPart(attachmentPart);
					}
					// 将multipart对象放到message中
					message.setContent(multipart);
				} else {
					message.setContent(mail.getContent(), mail.getType());
				}
				Transport.send(message);
				return Boolean.TRUE;
			} catch (Exception e) {
				logger.warn("fail to send email to " + to, e);
				return Boolean.FALSE;
			}
		}
	}
}
