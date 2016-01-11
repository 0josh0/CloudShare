package cn.ac.iscas.oncecloudshare.service.extensions.company.space.controller;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpace;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.service.CompanySpaceService;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.utils.CompanyUtils.ErrorCodes;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFile;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceFileFollowService;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceService;
import cn.ac.iscas.oncecloudshare.service.service.shiro.ShiroRealm;

import com.google.common.base.Function;

public class CompanySpaceBaseController extends BaseController {
	@Resource
	private ShiroRealm shiroRealm;
	@Resource
	protected SpaceService spaceService;
	@Resource
	protected CompanySpaceService companySpaceService;
	@Resource
	protected SpaceFileFollowService spaceFileFollowService;

	// 用于转换SpaceFile到Dto
	protected Function<SpaceFile, SpaceFileDto> fileToDto = new Function<SpaceFile, SpaceFileDto>() {
		public SpaceFileDto apply(SpaceFile input) {
			SpaceFileDto output = SpaceFileDto.defaultTransformer.apply(input);
			output.favorite = spaceFileFollowService.findOne(currentUserId(), input.getId()) != null;
			return output;
		}
	};

	@InitBinder({ "space", "file" })
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("abcdefg");
	}

	/**
	 * 获取访问的工作空间
	 * 
	 * @param workspaceId
	 * @return
	 */
	protected void initSpace(Model model) {
		CompanySpace space = companySpaceService.findOne();
		model.addAttribute("space", space);
	}

	protected void initFile(Model model, String fileId) {
		CompanySpace space = getSpace(model);
		SpaceFile file = null;
		if ("root".equals(fileId)) {
			file = spaceService.findRoot(space.getId());
		} else {
			file = spaceService.findFile(NumberUtils.toLong(fileId, -1));
		}
		if (!space.hasFile(file)) {
			throw new RestException(ErrorCodes.FILE_NOT_FOUND);
		}
		model.addAttribute("file", file);
	}

	protected CompanySpace getSpace(Model model) {
		return modelAttribute(model, "space");
	}

	protected SpaceFile getSpaceFileOrFolder(Model model) {
		return modelAttribute(model, "file");
	}

	protected SpaceFile getSpaceFolder(Model model) {
		SpaceFile file = getSpaceFileOrFolder(model);
		if (!file.getIsDir()) {
			throw new RestException(ErrorCodes.FOLDER_EXPECTED);
		}
		return file;
	}

	protected SpaceFile getSpaceFile(Model model) {
		SpaceFile file = getSpaceFileOrFolder(model);
		if (file.getIsDir()) {
			throw new RestException(ErrorCodes.FILE_EXPECTED);
		}
		return file;
	}

	@SuppressWarnings("unchecked")
	protected <T> T modelAttribute(Model model, String attributeName) {
		return (T) model.asMap().get(attributeName);
	}
}
