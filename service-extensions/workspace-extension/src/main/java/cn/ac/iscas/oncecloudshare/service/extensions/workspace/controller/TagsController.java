package cn.ac.iscas.oncecloudshare.service.extensions.workspace.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.CreateTagReq;
import cn.ac.iscas.oncecloudshare.service.dto.file.UpdateTagOrderReq;
import cn.ac.iscas.oncecloudshare.service.dto.space.TagDto;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.extensions.workspace.model.Workspace;
import cn.ac.iscas.oncecloudshare.service.model.common.SpaceTag;
import cn.ac.iscas.oncecloudshare.service.service.common.SpaceTagService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

@Controller("workspaceTagController")
@RequestMapping(value = "/api/{apiVer}/exts/workspaces/{workspaceId:\\d+}/tags", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class TagsController extends WorkspaceBaseController {
	@Resource
	private SpaceTagService spaceTagService;

	@ModelAttribute
	public void initModel(Model model, @PathVariable long workspaceId) {
		initWorkspace(model, workspaceId);
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String creatTag(@ModelAttribute("workspace") Workspace workspace, @Valid CreateTagReq req) {
		SpaceTag tag = spaceTagService.findOne(currentUserId(), req.getTitle());
		if (tag != null) {
			throw new RestException(ErrorCode.CONFLICT, "tag_title_already_exists");
		}
		tag = new SpaceTag();
		tag.setTitle(req.getTitle());
		tag.setOrderIndex(req.getOrderIndex());
		tag.setOwner(workspace.getSpace());
		tag = spaceTagService.save(tag);
		return gson().toJson(TagDto.Transformers.DEFAULT.apply(tag));
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String findAll(@ModelAttribute("workspace") Workspace workspace, @RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("owner.id", Operator.EQ, workspace.getSpace().getId()));
		if (StringUtils.isEmpty(pageParam.getSort())) {
			pageParam.setSort("orderIndex,createTime");
		}
		Page<SpaceTag> tags = spaceTagService.findAll(filters, pageParam.getPageable(SpaceTag.class));
		return Gsons.filterByFields(TagDto.class, pageParam.getFields()).toJson(PageDto.of(tags, TagDto.Transformers.DEFAULT));
	}

	@RequestMapping(value = "/orders", method = RequestMethod.PUT)
	@ResponseBody
	public String updateOrders(@ModelAttribute("workspace") Workspace workspace, @Valid UpdateTagOrderReq req) {
		if (req.getTags().length != req.getOrders().length) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		// 验证要编辑的tag是否都属于该工作空间
		List<SearchFilter> filters = Lists.newArrayList();
		filters.add(new SearchFilter("id", Operator.IN, req.getTags()));
		filters.add(new SearchFilter("owner.id", Operator.EQ, workspace.getSpace().getId()));
		List<SpaceTag> tags = spaceTagService.findAll(filters);
		if (tags.size() != req.getTags().length){
			throw new RestException(ErrorCode.NOT_FOUND, "tag_not_found");
		}
		
		spaceTagService.updateOrders(req);
		return ok();
	}

	@RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@ModelAttribute("workspace") Workspace workspace, @PathVariable long id) {
		SpaceTag tag = spaceTagService.findOne(id);
		if (tag == null || !tag.getOwner().equals(workspace.getSpace())) {
			throw new RestException(ErrorCode.NOT_FOUND, "tag_not_found");
		}
		spaceTagService.delete(tag);
		return ok();
	}
}
