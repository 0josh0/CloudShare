package cn.ac.iscas.oncecloudshare.service.extensions.company.space.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.dto.SpaceFileDto;
import cn.ac.iscas.oncecloudshare.service.extensions.company.space.model.CompanySpace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceFileFollow;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceFileFollowService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;

@Controller("companySpaceFileFollowController")
@RequestMapping(value = "/api/v2/exts/company/space", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class SpaceFileFollowController extends BaseController {
	@SuppressWarnings("unused")
	private static final Logger _logger = LoggerFactory.getLogger(SpaceFileFollowController.class);
	@Resource
	private SpaceFileFollowService spaceFileFollowService;

	/**
	 * 查看我的收藏
	 * 
	 * @param q
	 * @param pageParam
	 * @return
	 */
	@RequestMapping(value = "follows", method = RequestMethod.GET)
	@ResponseBody
	public String follows(PageParam pageParam) {
		Page<SpaceFileFollow> page = spaceFileFollowService.findAll(currentUserId(), CompanySpace.class,
				pageParam.getPageable(SpaceFileFollow.class));
		return Gsons.filterByFields(SpaceFileDto.class, pageParam.getFields()).toJson(PageDto.of(page, SpaceFileDto.followTransformer));
	}
}