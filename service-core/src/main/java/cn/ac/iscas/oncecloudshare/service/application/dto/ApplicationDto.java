package cn.ac.iscas.oncecloudshare.service.application.dto;

import cn.ac.iscas.oncecloudshare.service.application.model.Application;
import cn.ac.iscas.oncecloudshare.service.dto.account.UserDto;

import com.google.common.base.Preconditions;

public class ApplicationDto {
	public Long id;
	public String type;
	public String content;
	// 状态
	public String status;
	// 申请人
	public UserDto applyBy;
	// 申请时间
	public Long applyAt;
	// 审核人
	public UserDto reviewBy;
	// 审核时间
	public Long reviewAt;
	// 审核的意见
	public String reviewContent;
	
	public static <T extends ApplicationDto> T defaultInit(Application model, T dto){
		Preconditions.checkNotNull(model);
		Preconditions.checkNotNull(dto);
		dto.id = model.getId();
		// 申请相关信息
		if (model.getApplyBy() != null){
			dto.applyBy = UserDto.GLANCE_TRANSFORMER.apply(model.getApplyBy());
		}
		dto.applyAt = model.getApplyAt();
		dto.content = model.getContent();
		// 审核相关信息
		if (model.getReviewBy() != null){
			dto.reviewBy = UserDto.GLANCE_TRANSFORMER.apply(model.getReviewBy());
		}
		dto.reviewAt = model.getReviewAt();
		dto.reviewContent = model.getReviewContent();
		// 状态
		dto.status = model.getStatus() == null ? null : model.getStatus().name();
		return dto;
	}
}