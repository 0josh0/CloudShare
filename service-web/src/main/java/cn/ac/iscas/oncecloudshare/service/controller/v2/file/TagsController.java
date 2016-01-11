package cn.ac.iscas.oncecloudshare.service.controller.v2.file;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.controller.v2.BaseController;
import cn.ac.iscas.oncecloudshare.service.controller.v2.PageParam;
import cn.ac.iscas.oncecloudshare.service.dto.ErrorCode;
import cn.ac.iscas.oncecloudshare.service.dto.PageDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.CreateTagReq;
import cn.ac.iscas.oncecloudshare.service.dto.file.TagDto;
import cn.ac.iscas.oncecloudshare.service.dto.file.UpdateTagOrderReq;
import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.Tag;
import cn.ac.iscas.oncecloudshare.service.service.filemeta.TagService;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.service.utils.http.MediaTypes;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter;
import cn.ac.iscas.oncecloudshare.service.utils.jpa.SearchFilter.Operator;

@Controller
@RequestMapping(value = "/api/{apiVer}/tags", produces = { MediaTypes.TEXT_PLAIN_UTF8, MediaTypes.JSON_UTF8 })
public class TagsController extends BaseController {
	@Resource
	private TagService tagService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String creatTag(@Valid CreateTagReq req) {
		Tag tag = tagService.findOne(currentUserId(), req.getTitle());
		if (tag != null) {
			throw new RestException(ErrorCode.CONFLICT, "tag_title_already_exists");
		}
		tag = new Tag();
		tag.setTitle(req.getTitle());
		tag.setOrderIndex(req.getOrderIndex());
		tag.setOwner(currentUser());
		tag = tagService.save(tag);
		return gson().toJson(TagDto.DEFAULT_TRANSFORMER.apply(tag));
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public String findAll(@RequestParam(required = false) String q, PageParam pageParam) {
		List<SearchFilter> filters = decodeFilters(q);
		filters.add(new SearchFilter("owner.id", Operator.EQ, currentUserId()));
		if (StringUtils.isEmpty(pageParam.getSort())) {
			pageParam.setSort("orderIndex,createTime");
		}
		Page<Tag> tags = tagService.findAll(filters, pageParam.getPageable(Tag.class));
		return Gsons.filterByFields(TagDto.class, pageParam.getFields()).toJson(PageDto.of(tags, TagDto.DEFAULT_TRANSFORMER));
	}

	@RequestMapping(value = "/orders", method = RequestMethod.PUT)
	@ResponseBody
	public String updateOrders(@Valid UpdateTagOrderReq req) {
		if (req.getTags().length != req.getOrders().length) {
			throw new RestException(ErrorCode.BAD_REQUEST);
		}
		tagService.updateOrders(req);
		return ok();
	}

	@RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable long id) {
		Tag tag = tagService.findOne(id);
		if (tag == null || !tag.getOwner().equals(currentUser())) {
			throw new RestException(ErrorCode.NOT_FOUND, "tag_not_found");
		}
		tagService.delete(tag);
		return ok();
	}
}
