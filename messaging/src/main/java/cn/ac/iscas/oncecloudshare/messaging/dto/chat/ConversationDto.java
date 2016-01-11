package cn.ac.iscas.oncecloudshare.messaging.dto.chat;

import java.util.Date;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.messaging.model.chat.Conversation;


public final class ConversationDto {
	
	public static final Function<Conversation,ConversationDto> TRANSFORMER=
			new Function<Conversation,ConversationDto>(){

				@Override
				public ConversationDto apply(Conversation input){
					return new ConversationDto(input);
				}
			};

	public final Long userId;
	public final Long oppositeId;
	public final Long readSeq;
	public final Long maxSeq;
	public final Date updateTime;

	private ConversationDto(Conversation c){
		this.userId=c.getUserId();
		this.oppositeId=c.getOppositeId();
		this.readSeq=c.getReadSeq();
		this.maxSeq=c.getMaxSeq();
		this.updateTime=c.getUpdateTime();
	}
	
	public static ConversationDto of(Conversation c){
		return TRANSFORMER.apply(c);
	}
}
