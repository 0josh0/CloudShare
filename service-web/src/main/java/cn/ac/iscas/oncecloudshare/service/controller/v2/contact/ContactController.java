package cn.ac.iscas.oncecloudshare.service.controller.v2.contact;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.application.dto.ReviewApplication;
import cn.ac.iscas.oncecloudshare.service.application.model.ApplicationStatus;
import cn.ac.iscas.oncecloudshare.service.application.service.ApplicationService;
import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.NotifTypes;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;
import cn.ac.iscas.oncecloudshare.service.dto.contact.AddContactReq;
import cn.ac.iscas.oncecloudshare.service.dto.contact.ContactApplicationDto;
import cn.ac.iscas.oncecloudshare.service.dto.contact.ContactDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.account.UserStatus;
import cn.ac.iscas.oncecloudshare.service.model.contact.Contact;
import cn.ac.iscas.oncecloudshare.service.model.contact.ContactApplication;
import cn.ac.iscas.oncecloudshare.service.service.contact.ContactService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

@Controller
@RequestMapping(value = "/api/{apiVer}/contacts", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class ContactController extends BaseController {
	@Resource
	private ContactService contactService;
	@Resource
	private ApplicationService applicationService;

	/**
	 * 添加好友
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String add(@Valid AddContactReq req) {
		if (currentUserId() == req.getUserId()) {
			throw new RestException(ErrorCode.FORBIDDEN, "cannot_contact_yourself");
		}
		User target = uService.find(req.getUserId());
		if (target == null) {
			throw new RestException(ErrorCode.USER_NOT_FOUND);
		}
		if (!target.getStatus().equals(UserStatus.ACTIVE)) {
			throw new RestException(ErrorCode.USER_NOT_ACTIVE);
		}
		Contact contact = contactService.findOne(currentUserId(), req.getUserId());
		if (contact != null) {
			throw new RestException(ErrorCode.CONFLICT, "contact_already_exists");
		}
		// 查看要添加的用户是否也申请添加自己为好友
		ContactApplication application = contactService.findAvailableApplication(req.getUserId(), currentUserId());
		if (application != null) {
			applicationService.reviewApplication(application, new ReviewApplication(), currentUser());
			
			StringBuilder message = new StringBuilder().append(getUserPrincipal().getUserName()).append("通过了您的好友请求");
			sendNotif(NotifTypes.Contact.REVIEW, message.toString(), ContactApplicationDto.DEFAULT_TRANSFORMER.apply(application), target.getId());
			message.setLength(0);
			message.append("您通过了").append(application.getApplyBy().getName()).append("的好友请求");
			sendNotif(NotifTypes.Contact.REVIEW, message.toString(), ContactApplicationDto.DEFAULT_TRANSFORMER.apply(application), currentUserId());
			
			return ok();
		}
		// 创建申请
		application = new ContactApplication();
		application.setApplyBy(currentUser());
		application.setApplyAt(System.currentTimeMillis());
		application.setStatus(ApplicationStatus.TOREVIEW);
		application.setContentObject(req);
		application.setContact(target);
		applicationService.save(application);

		String message = new StringBuilder().append(getUserPrincipal().getUserName()).append("申请加您为好友").toString();
		sendNotif(NotifTypes.Contact.APPLY, message, ContactApplicationDto.DEFAULT_TRANSFORMER.apply(application),
				ImmutableList.<Long> of(target.getId()));

		return gson().toJson(ContactApplicationDto.DEFAULT_TRANSFORMER.apply(application));
	}

	/**
	 * 分页查询我的好友{"contact":{"id":1,"name":"005769","email":"005769@163.com","departmentName":"胖子工作室"},"id":501,"content":"{\"userId\":1,\"intro\":\"\"}","status":"TOREVIEW","applyBy":{"id":81,"name":"21","email":"21@21.cn","departmentName":"胖子工作室"},"applyAt":1402911608216}{"contact":{"id":1,"name":"005769","email":"005769@163.com","departmentName":"胖子工作室"},"id":501,"content":"{\"userId\":1,\"intro\":\"\"}","status":"TOREVIEW","applyBy":{"id":81,"name":"21","email":"21@21.cn","departmentName":"胖子工作室"},"applyAt":1402911608216}{"contact":{"id":1,"name":"005769","email":"005769@163.com","departmentName":"胖子工作室"},"id":501,"content":"{\"userId\":1,\"intro\":\"\"}","status":"TOREVIEW","applyBy":{"id":81,"name":"21","email":"21@21.cn","departmentName":"胖子工作室"},"applyAt":1402911608216}{"contact":{"id":1,"name":"005769","email":"005769@163.com","departmentName":"胖子工作室"},"id":501,"content":"{\"userId\":1,\"intro\":\"\"}","status":"TOREVIEW","applyBy":{"id":81,"name":"21","email":"21@21.cn","departmentName":"胖子工作室"},"applyAt":1402911608216}{"contact":{"id":1,"name":"005769","email":"005769@163.com","departmentName":"胖子工作室"},"id":501,"content":"{\"userId\":1,\"intro\":\"\"}","status":"TOREVIEW","applyBy":{"id":81,"name":"21","email":"21@21.cn","departmentName":"胖子工作室"},"applyAt":1402911608216}
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String page(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("owner.id", Operator.EQ, currentUserId()));
		Page<Contact> contacts = contactService.findAll(filters, pageParam.getPageable(Contact.class));
		return Gsons.filterByFields(ContactDto.class, pageParam.getFields()).toJson(PageDto.of(contacts, ContactDto.DEFAULT_TRANSFORMER));
	}

	/**
	 * 删除我的好友
	 * 
	 * @param contactId
	 * @return
	 */
	@RequestMapping(value = "/{contactId:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable long contactId) {
		Contact contact = contactService.findOne(contactId);
		if (contact == null || !contact.getOwner().getId().equals(currentUserId())) {
			throw new RestException(ErrorCode.NOT_FOUND, "contact_not_found");
		}
		contactService.delete(contact);

		Map<String, Object> attrs = getNotifAttrs(contact.getOwner(), contact.getContact());

		sendNotif(NotifTypes.Contact.DELETE, getUserPrincipal().getUserName().concat("取消了你们的好友关系"), attrs, contact.getContact().getId());
		sendNotif(NotifTypes.Contact.DELETE, "您取消了和" + contact.getContact().getName() + "的好友关系", attrs, currentUserId());

		return ok();
	}

	/**
	 * 审核添加好友申请
	 * 
	 * @param applyId 申请的id
	 * @param review 审核对象
	 * @return
	 */
	@RequestMapping(value = "applys/{applyId:\\d+}/review", method = RequestMethod.PUT)
	@ResponseBody
	public String review(@PathVariable("applyId") long applyId, @Valid ReviewApplication review) {
		ContactApplication application = applicationService.findOne(ContactApplication.class, applyId);
		if (application == null) {
			throw new RestException(ErrorCode.APPLICATION_NOT_FOUND);
		}
		if (!ApplicationStatus.TOREVIEW.equals(application.getStatus())) {
			throw new RestException(ErrorCode.APPLICATION_GONE);
		}
		applicationService.reviewApplication(application, review, currentUser());

		// 发送通知
		StringBuilder message = new StringBuilder().append(getUserPrincipal().getUserName()).append(review.getAgreed() ? "通过" : "拒绝")
				.append("了您的好友请求");
		sendNotif(NotifTypes.Contact.REVIEW, message.toString(), ContactApplicationDto.DEFAULT_TRANSFORMER.apply(application), application
				.getApplyBy().getId());
		message.setLength(0);
		message.append("您").append(review.getAgreed() ? "通过了" : "拒绝了").append(application.getApplyBy().getName()).append("的好友请求");
		sendNotif(NotifTypes.Contact.REVIEW, message.toString(), ContactApplicationDto.DEFAULT_TRANSFORMER.apply(application), currentUserId());

		Contact contact = contactService.findOne(currentUserId(), application.getContact().getId());
		if (contact != null) {
			return gson().toJson(ContactDto.DEFAULT_TRANSFORMER.apply(contact));
		}
		return ok();
	}

	/**
	 * 查询收到的好友申请
	 * 
	 * @param q 查询条件字符窜
	 * @param pageParam 分页参数
	 * @return
	 */
	@RequestMapping(value = "applys/recieved", method = RequestMethod.GET)
	@ResponseBody
	public String receivedApplications(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("contact.id", Operator.EQ, currentUserId()));
		Page<ContactApplication> page = applicationService.findApplications(ContactApplication.class, filters,
				pageParam.getPageable(ContactApplication.class));
		return Gsons.filterByFields(ContactApplicationDto.class, pageParam.getFields()).toJson(
				PageDto.of(page, ContactApplicationDto.DEFAULT_TRANSFORMER));
	}

	/**
	 * 查询发出的好友申请
	 * 
	 * @param q 查询条件字符窜
	 * @param pageParam 分页参数
	 * @return
	 */
	@RequestMapping(value = "applys/sended", method = RequestMethod.GET)
	@ResponseBody
	public String sendedApplications(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("applyBy.id", Operator.EQ, currentUserId()));
		Page<ContactApplication> page = applicationService.findApplications(ContactApplication.class, filters,
				pageParam.getPageable(ContactApplication.class));
		return Gsons.filterByFields(ContactApplicationDto.class, pageParam.getFields()).toJson(
				PageDto.of(page, ContactApplicationDto.DEFAULT_TRANSFORMER));
	}
	
	public Map<String, Object> getNotifAttrs(User master, User slave) {
		Map<String, Object> attrs = Maps.newHashMap();
		// master
		UserDto masterDto = new UserDto();
		masterDto.id = master.getId();
		masterDto.name = master.getName();
		attrs.put("master", masterDto);
		// slave
		UserDto slaveDto = new UserDto();
		slaveDto.id = slave.getId();
		slaveDto.name = slave.getName();
		attrs.put("slave", slaveDto);

		return attrs;
	}
}