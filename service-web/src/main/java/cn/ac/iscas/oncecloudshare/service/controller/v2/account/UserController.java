package cn.ac.iscas.oncecloudshare.service.controller.v2.account;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.ac.iscas.oncecloudshare.service.controller.annotation.AnonApi;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.MultipartFileByteSource;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserProfile;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.service.account.DepartmentService;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.primitives.Longs;

@Controller
@RequestMapping(value = "/api/v2/users", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class UserController extends BaseController {

	private static final long MAX_PORTRAIT_FILE_SIZE = 2 * 1024 * 1204;
	private static final Set<String> PORTRAIT_EXTS = ImmutableSet.of("jpg", "jpeg", "png");

	@Autowired()
	UserService uService;

	@Autowired
	DepartmentService dService;

	/**
	 * 查找user
	 * 
	 * @param id
	 *            数字id或者“me”
	 * @return
	 */
	private User findUser(String id) {
		if (id.equals("me")) {
			return currentUser();
		}
		Long userId = Longs.tryParse(id);
		if (userId == null) {
			throw new RestException(HttpStatus.BAD_REQUEST, "invalid user id");
		}
		User user = uService.find(userId);
		if (user == null) {
			throw new RestException(ErrorCode.USER_NOT_FOUND);
		}
		return user;
	}

	/**设置用户签名
	 * @param id
	 * @param signature
	 * @return
	 */
	@RequestMapping(value = "/{id:me|\\d+}/signature", method = RequestMethod.POST)
	@ResponseBody
	public String setSignature(@PathVariable String id, @RequestParam(required = false) String signature) {
		User user = findUser(id);
		if(user==null)
		{
			throw new RestException(ErrorCode.USER_NOT_FOUND);
		}
		if (!StringUtils.isEmpty(signature))
			uService.saveUserSignature(signature, user.getId());
		return ok();
	}

	@AnonApi
	@RequestMapping(value = "registration", method = RequestMethod.POST)
	@ResponseBody
	public String register(@RequestParam String name, @RequestParam String email, @RequestParam String password,
			@RequestParam(required = false) Long departmentId) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPlainPassword(password);
		user.setDepartment(null);
		user.setQuota(globalConfigService.getConfigAsLong(Configs.Keys.USER_QUOTA, Configs.Defaults.USER_QUOTA));
		if (departmentId != null) {
			user.setDepartment(dService.find(departmentId));
		}
		UserStatus status = UserStatus.ACTIVE;
		if (globalConfigService.getConfigAsBoolean(Configs.Keys.REG_NEED_APPROVAL, false)) {
			status = UserStatus.APPLYING;
		}
		user.setStatus(status);
		uService.addUser(user);
		if (UserStatus.ACTIVE.equals(status)) {
			uService.sendRegistrationMail(user);
		}
		return gson().toJson(UserDto.forOwner(user));
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String list(PageParam pageParam) {
		Page<User> page = uService.findAll(pageParam.getPageable(User.class));
		return Gsons.filterByFields(User.class, pageParam.getFields()).toJson(PageDto.of(page, UserDto.ANON_TRANSFORMER));
	}

	@RequestMapping(value = "{id:me|\\d+}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String id) {
		User user = findUser(id);
		return gson().toJson(user.getId().equals(currentUserId()) ? UserDto.forOwner(user) : UserDto.forAnon(user));
	}

	@RequestMapping(value = "me", method = RequestMethod.PUT)
	@ResponseBody
	public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
		User user = currentUser();
		if (!uService.verifyPassword(user, oldPassword)) {
			throw new RestException(ErrorCode.WRONG_OLD_PASSWORD);
		}
		uService.changePassword(user.getId(), newPassword);
		return gson().toJson(ResponseDto.OK);
	}

	@AnonApi
	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public String search(@RequestParam String q, @RequestParam(required = false) String o, PageParam pageParam) {
		List<SearchFilter> filters = SearchFilter.parseQuery(q);
		if (!isAuthenticatedUser()) {
			// 如果没有登录，只能按email查询
			filters = Lists.newArrayList(Iterables.filter(filters, new Predicate<SearchFilter>() {
				@Override
				public boolean apply(SearchFilter input) {
					return input.fieldName.equals("email") && input.operator == Operator.EQ;
				}
			}));
			if (filters.isEmpty()) {
				filters.add(new SearchFilter("id", Operator.EQ, -1));
			}
			pageParam.setFields(Lists.newArrayList("email"));
			o = null;
		}
		List<SearchFilter> or = StringUtils.isEmpty(o) ? new ArrayList<SearchFilter>() : SearchFilter.parseQuery(o);
		Page<User> page = uService.findAll(filters, or, pageParam.getPageable(User.class));
		return Gsons.filterByFields(UserDto.class, pageParam.getFields()).toJson(PageDto.of(page, UserDto.ANON_TRANSFORMER));
	}

	@RequestMapping(value = "me/profile", method = RequestMethod.PUT)
	@ResponseBody
	public String updateProfile(UserProfile newProfile) {
		User user = currentUser();
		return gson().toJson(uService.updateProfile(user, newProfile));
	}

	@RequestMapping(value = "{id:me|\\d+}/profile", method = RequestMethod.GET)
	@ResponseBody
	public String getProfile(@PathVariable String id) {
		User user = findUser(id);
		return gson().toJson(user.getProfile());
	}

	@RequestMapping(value = "me/avatar", method = RequestMethod.POST, params = "!base64", headers = "content-type=multipart/*")
	@ResponseBody
	public String uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {

		String ext = FilenameUtils.getExtension(file.getOriginalFilename());
		if (file == null || file.getSize() <= 0 || file.getSize() > MAX_PORTRAIT_FILE_SIZE || PORTRAIT_EXTS.contains(ext) == false) {
			throw new RestException(ErrorCode.INVALID_AVATAR_FILE);
		}
		uService.getAvatarHelper().updateAvatar(currentUser(), new MultipartFileByteSource(file));
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "me/avatar", method = RequestMethod.POST, params = "base64")
	@ResponseBody
	public String uploadAvatarByBase64(@RequestParam final String base64) throws IOException {
		ByteSource source = new ByteSource() {

			@Override
			public InputStream openStream() throws IOException {
				byte[] bytes = Base64.decodeBase64(base64);
				return new ByteArrayInputStream(bytes);
			}
		};
		uService.getAvatarHelper().updateAvatar(currentUser(), source);
		return gson().toJson(ResponseDto.OK);
	}

	@RequestMapping(value = "{id:me|\\d+}/avatar", method = RequestMethod.GET)
	public ResponseEntity<?> downloadAvatar(HttpServletRequest request, HttpServletResponse response, @PathVariable String id,
			@RequestParam(required = false, defaultValue = "small") String size) throws IOException {
		User user = findUser(id);
		String md5 = null;
		if (size.equals("large")) {
			md5 = uService.getAvatarHelper().getLargeAvatarMd5(user);
		} else if (size.equals("middle")) {
			md5 = uService.getAvatarHelper().getMiddleAvatarMd5(user);
		} else {
			md5 = uService.getAvatarHelper().getSmallAvatarMd5(user);
		}
		if (md5 == null) {
			throw new RestException(ErrorCode.AVATAR_NOT_FOUND);
		}

		setETag(response, md5);

		if (matchETagStrong(request, md5)) {
			return NOT_MODIFIED;
		}

		ByteSource source = runtimeContext.getFileStorageService().retrieveFileContent(md5);
		if (source != null) {
			initDownload(request, response, source, null);
		}
		return OK;
	}
}
