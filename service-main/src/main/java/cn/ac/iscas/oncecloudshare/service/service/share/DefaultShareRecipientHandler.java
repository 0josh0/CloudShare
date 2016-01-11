package cn.ac.iscas.oncecloudshare.service.service.share;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import cn.ac.iscas.oncecloudshare.service.model.account.Team;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.model.common.TeamMate;
import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.model.share.ShareRecipient;
import cn.ac.iscas.oncecloudshare.service.service.account.DepartmentService;
import cn.ac.iscas.oncecloudshare.service.service.account.TeamService;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Component
public class DefaultShareRecipientHandler implements ShareRecipientHandler {
	private static final Logger _logger = LoggerFactory.getLogger(DefaultShareRecipientHandler.class);

	public static class SupportedTypes {
		public static final String USER = "user";
		public static final String DEPARTMENT = "department";
		public static final String TEAM = "team";
		public static final String EMAIL = "email";
		public static final List<String> ALL = ImmutableList.of(USER, DEPARTMENT, TEAM, EMAIL);
	}

	@Resource
	private UserService userService;
	@Resource
	private DepartmentService departmentService;
	@Resource
	private TeamService teamService;
	@Resource
	private MailService mailService;
	@Resource
	private RuntimeContext runtimeContext;

	@Override
	public List<String> getSupportedRecipientTypes() {
		return SupportedTypes.ALL;
	}

	@Override
	public List<ReceivedShare> generateReceivedShares(Share share, ShareRecipient target) {
		if (SupportedTypes.USER.equals(target.getType())) {
			return generateUserReceivedShares(share, target);
		} else if (SupportedTypes.DEPARTMENT.equals(target.getType())) {
			return generateDepartmentReceivedShares(share, target);
		} else if (SupportedTypes.TEAM.equals(target.getType())) {
			return generateTeamReceivedShares(share, target);
		} else if (SupportedTypes.EMAIL.equals(target.getType())) {
			return generateEmailReceivedShares(share, target);
		}
		throw new UnsupportedOperationException("不支持的分享对象类型:" + target.getType());
	}

	protected List<ReceivedShare> generateUserReceivedShares(Share share, ShareRecipient recipient) {
		List<ReceivedShare> results = Lists.newArrayList();
		User user = userService.find(recipient.getIdentify());
		if (user == null) {
			_logger.warn("不能生成RecievedShare对象，原因：找不到用户" + recipient.getIdentify());
			return results;
		}
		recipient.setDisplayName(user.getName());
		ReceivedShare receivedShare = new ReceivedShare();
		receivedShare.setShare(share);
		receivedShare.setRecipient(user);
		receivedShare.setBelongsTo(Lists.newArrayList(recipient));
		results.add(receivedShare);
		return results;
	}

	protected List<ReceivedShare> generateDepartmentReceivedShares(Share share, ShareRecipient recipient) {
		List<ReceivedShare> results = Lists.newArrayList();
		Department department = departmentService.find(recipient.getIdentify());
		if (department == null) {
			_logger.warn("不能生成RecievedShare对象，原因：找不到部门：" + recipient.getIdentify());
			return results;
		}
		recipient.setDisplayName(department.getName());
		List<User> users = userService.findAllByDeparmtent(department, UserStatus.ACTIVE);
		for (User user : users) {
			ReceivedShare receivedShare = new ReceivedShare();
			receivedShare.setShare(share);
			receivedShare.setRecipient(user);
			receivedShare.setBelongsTo(Lists.newArrayList(recipient));
			results.add(receivedShare);
		}
		return results;
	}

	protected List<ReceivedShare> generateTeamReceivedShares(Share share, ShareRecipient recipient) {
		List<ReceivedShare> results = Lists.newArrayList();
		Team team = teamService.findOne(recipient.getIdentify());
		if (team == null) {
			_logger.warn("不能生成RecievedShare对象，原因：找不到team：" + recipient.getIdentify());
			return results;
		}
		recipient.setDisplayName(team.getName());
		for (TeamMate mate : team.getMembers()) {
			ReceivedShare receivedShare = new ReceivedShare();
			receivedShare.setShare(share);
			receivedShare.setRecipient(mate.getUser());
			receivedShare.setBelongsTo(Lists.newArrayList(recipient));
			results.add(receivedShare);
		}
		return results;
	}

	protected List<ReceivedShare> generateEmailReceivedShares(Share share, ShareRecipient recipient) {
		String subject = share.getCreator().getName() + "给你分享了" + share.getFile().getName();
		Mail mail = new Mail(subject, share.getDescription());
		try {
			mail.addAttachment(share.getFile().getName(),
					runtimeContext.getFileStorageService().retrieveFileContent(share.getFile().getHeadVersion().getMd5()));
			mailService.send(recipient.getDisplayName(), mail);
		} catch (IOException e) {
			_logger.error(null, e);
		}
		return Lists.newArrayList();
	}
}
