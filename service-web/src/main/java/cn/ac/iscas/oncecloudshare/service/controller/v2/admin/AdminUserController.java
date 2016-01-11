package cn.ac.iscas.oncecloudshare.service.controller.v2.admin;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto.BatchReviewRequest;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.Department;
import cn.ac.iscas.oncecloudshare.service.model.account.Role;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail.EmailParameter;
import cn.ac.iscas.oncecloudshare.service.model.common.TempItem;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.Tenant;
import cn.ac.iscas.oncecloudshare.service.service.account.DepartmentService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;
import cn.ac.iscas.oncecloudshare.service.service.common.TempItemService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;

/**
 * adminapi:user相关功能，包括：
 * 
 * <pre>
 * 获取所有用户    GET		/adminapi/v2/users
 * 查询用户    GET		/adminapi/v2/users/search
 * </pre>
 * 
 * @author One
 * @version
 * @since JDK 1.6
 */
@Controller
@RequestMapping(value = "/adminapi/v2/users", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class AdminUserController extends BaseController {

	/**
	 * 邀请用户
	 */
	public static final String INVITE_TEMP_ITEM_TYPE = "invite";

	/**
	 * 过期时间
	 */
	public static final long INVITE_EXPIRE_TIME = Integer.MAX_VALUE;

	@Autowired
	DepartmentService dService;

	@Autowired
	MailService mailService;

	@Autowired
	TempItemService tiService;

	private User findUser(Long id) {
		User user = uService.find(id);
		if (user == null) {
			throw new RestException(ErrorCode.USER_NOT_FOUND);
		}
		return user;
	}

	@RequestMapping(value = { "", "search" }, method = RequestMethod.GET)
	@ResponseBody
	public String list(@RequestParam(required = false) String q, @RequestParam(required = false) String o, PageParam pageParam) {
		List<SearchFilter> and = StringUtils.isEmpty(q) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(q);
		List<SearchFilter> or = StringUtils.isEmpty(o) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(o);
		Page<User> page = uService.findAll(and, or, pageParam.getPageable(User.class));
		return Gsons.filterByFields(User.class, pageParam.getFields()).toJson(PageDto.of(page, UserDto.ADMIN_TRANSFORMER));
	}

	/**
	 * 批量添加用户，body中以JsonArray的格式保存用户数据，
	 * 可以包含的字段包括name、email、plainPassword、departmentId
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String batchAddUser(@RequestBody String requestBody) {

		List<UserDto.Request> list = null;
		try {
			@SuppressWarnings("serial")
			Type type = new TypeToken<List<UserDto.Request>>() {
			}.getType();
			list = gson().fromJson(requestBody, type);
		} catch (JsonSyntaxException e) {
			throw new RestException(ErrorCode.BAD_REQUEST, "invalid request body");
		}


		List<User> userList = Lists.newArrayList();
		for (UserDto.Request r : list) {
			User user = new User();
			user.setName(r.name);
			user.setEmail(r.email);
			user.setPlainPassword(r.plainPasword);
			user.setDepartment(null);
			if (r.departmentId != null) {
				user.setDepartment(dService.find(r.departmentId));
			}
			if (r.quota != null && r.quota > 0) {
				user.setQuota(r.quota);
			} else {
				user.setQuota(globalConfigService.getConfigAsLong(Configs.Keys.USER_QUOTA, Configs.Defaults.USER_QUOTA));
			}
			UserStatus status = UserStatus.ACTIVE;
			user.setStatus(status);
			userList.add(user);
		}
		uService.batchAddUser(userList);
		return ok();
	}

	/**
	 * 批量邀请用户，body中以JsonArray的格式保存用户数据， 可以包含的字段包括name、email、departmentId、quota
	 */
	@RequestMapping(value = "invitation", method = RequestMethod.POST)
	@ResponseBody
	public String batchInvite(@RequestBody String requestBody) {
		List<UserDto.BatchInviteRequest> list = null;

		String subject = globalConfigService.getConfig(Configs.Keys.MAIL_INVITATION_SUBJECT, "");
		System.out.println(subject);
		try {
			@SuppressWarnings("serial")
			Type type = new TypeToken<List<UserDto.BatchInviteRequest>>() {
			}.getType();
			list = gson().fromJson(requestBody, type);
		} catch (JsonSyntaxException e) {
			throw new RestException(ErrorCode.BAD_REQUEST, "invalid request body");
		}
		List<User> userList = Lists.transform(list, new Function<UserDto.BatchInviteRequest, User>() {

			@Override
			public User apply(UserDto.BatchInviteRequest r) {
				User user = new User();
				String name = Strings.isNullOrEmpty(r.name) ? "云享用户" : r.name;
				user.setName(name);
				user.setEmail(r.email);
				user.setPlainPassword("111111");
				user.setDepartment(null);
				if (r.departmentId != null) {
					user.setDepartment(dService.find(r.departmentId));
				}
				if (r.quota != null && r.quota > 0) {
					user.setQuota(r.quota);
				} else {
					user.setQuota(globalConfigService.getConfigAsLong(Configs.Keys.USER_QUOTA, Configs.Defaults.USER_QUOTA));
				}
				user.setStatus(UserStatus.UNACTIVATED);
				return user;
			}
		});

		uService.batchAddUser(userList);

		sendActivateEmailsToAll(userList);

		return ok();
	}

	private void sendActivateEmailsToAll(List<User> userList) {
		for (User user : userList) {
			TempItem ti = tiService.save(INVITE_TEMP_ITEM_TYPE, user.getEmail(), INVITE_EXPIRE_TIME);
			sendActivateEmail(user, ti);
		}
	}

	private void sendActivateEmail(User user, TempItem ti) {

		Tenant currTenant = runtimeContext.getTenantService().getCurrentTenant();
		long cuurentTenantId = this.runtimeContext.getTenantService().getCurrentTenant().getId();

		String subject = globalConfigService.getConfig(Configs.Keys.MAIL_INVITATION_SUBJECT, "");
		String content = globalConfigService.getConfig(Configs.Keys.MAIL_INVITATION_CONTENT, "");
		StringBuilder url = new StringBuilder();

		url.append(globalConfigService.getConfig(Configs.Keys.CLIENT_WEB_URL, ""));

		url.append(globalConfigService.getConfig(Configs.Keys.ACTIVATE_URL, ""));

		List<EmailParameter> parameters = Lists.newArrayList(new EmailParameter("tenant_name", currTenant.getName()), new EmailParameter(
				"activate_url", url.toString()), new EmailParameter("token", ti.getKey()),
				new EmailParameter("x-tenant-id", currTenant.getId()));

		this.runtimeContext.getTenantService().setCurrentTenant(cuurentTenantId);

		Mail mail = new Mail(subject, content, parameters);

		mailService.send(user.getEmail(), mail);

	}

	/**
	 * 更新用户数据
	 */
	@RequestMapping(value = "{id:\\d+}", method = RequestMethod.PUT)
	@ResponseBody
	public String updateUser(@PathVariable Long id, @RequestParam(required = false) String name,
			@RequestParam(required = false) String plainPassword, @RequestParam(required = false) Long quota,
			@RequestParam(required = false) String departmentId, @RequestParam(required = false) String status) {
		User user = findUser(id);

		// 如果参数不包含departmentId，则不更新
		// 如果为空字符串，则把department置为null
		// 否则设置为相应的department
		Department d = null;
		if (departmentId == null) {
			d = user.getDepartment();
		} else if (departmentId.trim().isEmpty()) {
			d = null;
		} else {
			d = dService.find(Longs.tryParse(departmentId));
			Preconditions.checkArgument(d != null, "no such department");
		}

		UserStatus us = UserStatus.of(status);
		user = uService.updateUser(id, name, plainPassword, quota, d, us);
		return gson().toJson(UserDto.forAdmin(user));
	}

	@RequestMapping(params = "review", method = RequestMethod.PUT)
	@ResponseBody
	public String batchReview(@Valid BatchReviewRequest request) {
		List<UserDto.ReviewResponse> responses = Lists.newArrayList();
		for (long id : request.getIds()) {
			UserDto.ReviewResponse response = new UserDto.ReviewResponse();
			responses.add(response);
			response.userId = id;

			User user = uService.find(id);
			if (user == null) {
				response.success = false;
				response.errorCode = ErrorCode.USER_NOT_FOUND;
			} else if (!UserStatus.APPLYING.equals(user.getStatus())) {
				response.success = false;
				response.errorCode = new ErrorCode(ErrorCode.USER_NOT_APPLYING.statusCode, ErrorCode.USER_NOT_APPLYING.subCode, user
						.getStatus().name());
			} else {
				response.success = true;
				uService.updateUser(id, null, null, null, null, request.isAgreed() ? UserStatus.ACTIVE : UserStatus.FROZEN);
				if (request.isAgreed()) {
					uService.sendRegistrationMail(user);
				}
			}
		}
		return gson().toJson(responses);
	}

	/**
	 * 删除用户
	 */
	@RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteUser(@PathVariable Long id) {
		uService.deleteUser(id);
		return ok();
	}

	/**
	 * 添加角色
	 */
	@RequestMapping(value = "{id:\\d+}/roles/{role:[^:]+:[^:]+}", method = RequestMethod.POST)
	@ResponseBody
	public String addRole(@PathVariable long id, @PathVariable String role) {
		User user = findUser(id);
		String[] arr = role.split(":", 2);
		uService.addRole(user, new Role(arr[0], arr[1]));
		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 删除角色
	 */
	@RequestMapping(value = "{id:\\d+}/roles/{role:[^:]+:[^:]+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteRole(@PathVariable long id, @PathVariable String role) {
		User user = findUser(id);
		String[] arr = role.split(":", 2);
		uService.deleteRole(user, new Role(arr[0], arr[1]));
		return gson().toJson(ResponseDto.OK);
	}
}
