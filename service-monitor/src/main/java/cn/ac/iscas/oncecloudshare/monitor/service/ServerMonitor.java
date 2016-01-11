package cn.ac.iscas.oncecloudshare.monitor.service;

import java.net.Socket;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ac.iscas.oncecloudshare.monitor.dao.ServerDao;
import cn.ac.iscas.oncecloudshare.monitor.model.Server;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail.EmailParameter;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;

import com.google.common.collect.Lists;

@Service
@Transactional(readOnly = true)
public class ServerMonitor {
	private static final Logger _logger = LoggerFactory.getLogger(ServerMonitor.class);
	
	@Resource
	private ServerDao serverDao;
	@Resource
	private MailService mailService;
	@Resource
	private ConfigService configService;

	public Iterable<Server> findAll() {
		return serverDao.findAll();
	}

	public Iterable<Server> findAll(Pageable page) {
		return serverDao.findAll(page);
	}

	@Transactional(readOnly = false)
	@Scheduled(cron="0 0/1 * * * ?")
	public synchronized void check() {
		_logger.debug("定时任务");
		for (Server server : findAll()) {
			check(server);
		}
	}

	protected boolean check(Server server) {
		Boolean crashed = Boolean.FALSE;
		Socket socket = null;
		try {
			socket = new Socket(server.getHost(), server.getPort());
		} catch (Exception e) {
			crashed = Boolean.TRUE;
		} finally {
			IOUtils.closeQuietly(socket);
		}
		// 当状态发生变化
		if (!server.getCrashed().equals(crashed)) {
			server.setCrashed(crashed);
			server.setCheckTime(System.currentTimeMillis());
			serverDao.save(server);

			// 发送邮件通知用户
			String to = configService.getConfig("monitor.admin.email", StringUtils.EMPTY);
			if (StringUtils.isNotEmpty(to)) {
				List<EmailParameter> parameters = Lists.newArrayList(new EmailParameter("server", server.getName()), new EmailParameter("status",
						crashed ? "挂了" : "启动了"));

				String subject = configService.getConfig("monitor.email.status.subject", StringUtils.EMPTY);
				String content = configService.getConfig("monitor.email.status.content", StringUtils.EMPTY);
				Mail mail = new Mail(subject, content, parameters);
				mailService.send(to, mail);
			}
		}
		return crashed;
	}

	@Transactional(readOnly = false)
	public Server create(Server server) {
		return serverDao.save(server);
	}
}
