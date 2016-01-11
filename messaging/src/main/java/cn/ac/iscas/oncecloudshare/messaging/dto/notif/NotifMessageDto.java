package cn.ac.iscas.oncecloudshare.messaging.dto.notif;

import cn.ac.iscas.oncecloudshare.messaging.model.notif.NotifMessage;

import com.google.common.base.Function;


public final class NotifMessageDto {
	
	public static final Function<NotifMessage,NotifMessageDto> TRANSFORMER=
			new Function<NotifMessage,NotifMessageDto>(){

				@Override
				public NotifMessageDto apply(NotifMessage input){
					return new NotifMessageDto(input);
				}
			};

	public final Long id;
	public final String type;
	public final String content;
	public final Long ts;
	public final Long receiver;
	public final Boolean readFlag;
	public final String attributes;
	
	private NotifMessageDto(NotifMessage message){
		id=message.getId();
		type=message.getType();
		content=message.getContent();
		ts=message.getTs();
		receiver=message.getReceiver();
		readFlag=message.getReadFlag();
		attributes=message.getAttributes();
	}
	
	public static NotifMessageDto of(NotifMessage message){
		return TRANSFORMER.apply(message);
	}
}
