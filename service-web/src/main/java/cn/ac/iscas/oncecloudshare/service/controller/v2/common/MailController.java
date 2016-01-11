package cn.ac.iscas.oncecloudshare.service.controller.v2.common;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller
@RequestMapping(value = "/api/{apiVer}/mails", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class MailController extends BaseController {
	@Resource
	private MailService mailService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String sendMail(@Valid Mail mail, @RequestParam String to) {
		if (to.length() == 0) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		mailService.send(to, mail);
		return ok();
	}
}