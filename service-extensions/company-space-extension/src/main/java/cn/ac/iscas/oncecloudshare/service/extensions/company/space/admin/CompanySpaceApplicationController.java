package cn.ac.iscas.oncecloudshare.service.extensions.company.space.admin;

import java.io.IOException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.filemeta.InsufficientQuotaException;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.controller.CompanySpaceBaseController;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.ApplicationDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpace;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpaceApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.UploadApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.UploadVersionApplication;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.service.CompanySpaceService;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

import com.google.common.base.Strings;

@Controller("adminCompanySpaceApplicationController")
@RequestMapping(value = "/adminapi/v2/exts/company/space/applications", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class CompanySpaceApplicationController extends CompanySpaceBaseController {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CompanySpaceApplicationController.class);

	@Resource
	private CompanySpaceService companySpaceService;

	@ModelAttribute
	public void initModel(Model model/* , @PathVariable("appId") long applicationId */) {
		initSpace(model);
		// CompanySpaceApplication application = companySpaceService.findApplication(applicationId);
		// if (application == null) {
		// throw new RestException(ErrorCode.NOT_FOUND);
		// }
		// model.addAttribute("application", application);
	}

	/**
	 * 审核工作空间申请
	 * 
	 * @param applicationId
	 * @param reviewRequest
	 * @return
	 */
	@RequestMapping(value = "upload/{appId}", method = RequestMethod.PUT)
	@ResponseBody
	public String review(@ModelAttribute("space") CompanySpace space, @PathVariable("appId") long appId, ApplicationDto.UploadReview reviewRequest) {
		CompanySpaceApplication application = companySpaceService.findApplication(appId);
		if (application == null || !(application instanceof UploadApplication)) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		UploadApplication app = (UploadApplication) application;
		SpaceFile parent = null;
		String name = null;
		if (reviewRequest.getAgreed()) {
			parent = reviewRequest.getParentId() == null ? app.getTargetFolder() : spaceService.findFolder(space, reviewRequest.getParentId());
			if (parent == null) {
				throw new RestException(ErrorCode.FILE_NOT_FOUND);
			}
			name = Strings.isNullOrEmpty(reviewRequest.getName()) ? app.getUploadedFile().getName() : reviewRequest.getName();
		}
		companySpaceService.reivewApplication(currentUser(), app, reviewRequest.getAgreed(), parent, name, reviewRequest.getMessage());

		return gson().toJson(ResponseDto.OK);
	}

	/**
	 * 审核工作空间申请
	 * 
	 * @param applicationId
	 * @param reviewRequest
	 * @return
	 * @throws IOException
	 * @throws InsufficientQuotaException
	 */
	@RequestMapping(value = "uploadVersion/{appId}", method = RequestMethod.PUT)
	@ResponseBody
	public String review(@PathVariable("appId") long appId, ApplicationDto.UploadVersionReview reviewRequest) throws InsufficientQuotaException,
			IOException {
		CompanySpaceApplication application = companySpaceService.findApplication(appId);
		if (application == null || !(application instanceof UploadVersionApplication)) {
			throw new RestException(ErrorCode.NOT_FOUND);
		}
		UploadVersionApplication app = (UploadVersionApplication) application;
		companySpaceService.reivewApplication(currentUser(), app, reviewRequest.getAgreed(), reviewRequest.getMessage());

		return gson().toJson(ResponseDto.OK);
	}
}
